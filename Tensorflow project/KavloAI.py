import numpy as np
import pandas as pd
from scipy.signal import butter, filtfilt
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from tensorflow.keras import layers, models
import tensorflow as tf

# -----------------------------
# 1. Load Dataset
# -----------------------------
DATA_CSV = "your_dataset.csv"   # CHANGE HERE: your CSV path
df = pd.read_csv(DATA_CSV)

# -----------------------------
# 2. Select Columns
# -----------------------------
required_cols = ["heart_rate", "spo2", "gsr", "skin_temp", 
                 "acc_x", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z"]  # CHANGE HERE if headers differ
LABEL_COLUMN = "fatigue_label"   # CHANGE HERE: adjust if your label column name is different
df = df[required_cols + [LABEL_COLUMN]]

# -----------------------------
# 3. Sampling Frequency
# -----------------------------
FS = 1   # CHANGE HERE: Hz, depends on your device logging frequency

# -----------------------------
# 4. Artifact Reduction
# -----------------------------
# Remove invalid ranges
df = df[(df['spo2'] > 80) & (df['spo2'] < 100)]  # CHANGE HERE thresholds if needed
df = df[(df['heart_rate'] > 40) & (df['heart_rate'] < 180)]

# Reject sudden jumps in HR
HR_JUMP_THRESHOLD = 20   # CHANGE HERE if device is noisier
df['hr_diff'] = df['heart_rate'].diff().abs()
df = df[df['hr_diff'] < HR_JUMP_THRESHOLD]
df.drop(columns=['hr_diff'], inplace=True)

# Interpolation for small gaps
df = df.interpolate(limit=5)  # CHANGE HERE depending on missing data

# -----------------------------
# 5. Filtering
# -----------------------------
def butter_lowpass_filter(data, cutoff, fs, order=4):
    nyq = 0.5 * fs
    normal_cutoff = cutoff / nyq
    b, a = butter(order, normal_cutoff, btype='low', analog=False)
    return filtfilt(b, a, data)

df['heart_rate'] = butter_lowpass_filter(df['heart_rate'], cutoff=0.4, fs=FS)
df['gsr'] = butter_lowpass_filter(df['gsr'], cutoff=0.4, fs=FS)
df['skin_temp'] = butter_lowpass_filter(df['skin_temp'], cutoff=0.2, fs=FS)

# -----------------------------
# 6. Feature Engineering
# -----------------------------
WINDOW_SECONDS = 10  # CHANGE HERE: window size in seconds
WINDOW_SIZE = FS * WINDOW_SECONDS

def extract_features(window):
    return pd.Series({
        "hr_mean": window["heart_rate"].mean(),
        "hr_std": window["heart_rate"].std(),
        "spo2_mean": window["spo2"].mean(),
        "gsr_mean": window["gsr"].mean(),
        "temp_mean": window["skin_temp"].mean(),
        "acc_mag": np.sqrt((window["acc_x"]**2 + window["acc_y"]**2 + window["acc_z"]**2).mean()),
        "gyro_mag": np.sqrt((window["gyro_x"]**2 + window["gyro_y"]**2 + window["gyro_z"]**2).mean()),
    })

features = []
labels = []
for i in range(0, len(df) - WINDOW_SIZE, WINDOW_SIZE):
    window = df.iloc[i:i+WINDOW_SIZE]
    feat = extract_features(window)
    features.append(feat)
    labels.append(window[LABEL_COLUMN].mode()[0])

X = pd.DataFrame(features)
y = np.array(labels)

# -----------------------------
# 7. Normalize
# -----------------------------
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# -----------------------------
# 8. Train-Test Split
# -----------------------------
X_train, X_test, y_train, y_test = train_test_split(
    X_scaled, y, test_size=0.2, random_state=42
)

# -----------------------------
# 9. Build Model
# -----------------------------
model = models.Sequential([
    layers.Input(shape=(X_train.shape[1],)),
    layers.Dense(64, activation='relu'),
    layers.Dropout(0.3),
    layers.Dense(32, activation='relu'),
    layers.Dense(1, activation='linear')  # output fatigue score 0–100
])

model.compile(optimizer='adam', loss='mse', metrics=['mae'])
model.fit(X_train, y_train, validation_data=(X_test, y_test), epochs=20, batch_size=32)

# -----------------------------
# 10. Convert to TFLite
# -----------------------------
def representative_dataset_gen():
    for i in range(100):  # CHANGE HERE: number of calibration samples
        yield [X_train[i:i+1].astype(np.float32)]

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.representative_dataset = representative_dataset_gen
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
converter.inference_input_type = tf.uint8
converter.inference_output_type = tf.uint8

tflite_model = converter.convert()

with open("fatigue_model.tflite", "wb") as f:
    f.write(tflite_model)
