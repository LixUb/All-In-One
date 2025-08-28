package kavlo.sft.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kavlo.sft.mobile.ui.theme.SmartHeadbandTheme
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random
import kavlo.sft.mobile.service.ESP32BluetoothService
import kavlo.sft.mobile.data.SensorData
import androidx.compose.runtime.collectAsState
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private lateinit var esp32Service: ESP32BluetoothService

    // Permission launcher untuk Bluetooth permissions
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.values.all { it }
        if (allPermissionsGranted) {
            // Semua permission diberikan, coba connect
            esp32Service.connectToDevice("XX:XX:XX:XX:XX:XX") // Ganti dengan MAC address ESP32 yang benar
        } else {
            Toast.makeText(this, "Bluetooth permissions required for device connection", Toast.LENGTH_LONG).show()
        }
    }

    // Bluetooth enable launcher
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth enabled, request permissions
            requestBluetoothPermissions()
        } else {
            Toast.makeText(this, "Bluetooth is required for device connection", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        esp32Service = ESP32BluetoothService(this)

        setContent {
            SmartHeadbandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartHeadbandApp(esp32Service)
                }
            }
        }

        // Check dan request permissions saat app start
        checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        esp32Service.cleanup()
    }

    private fun checkAndRequestPermissions() {
        // Check apakah Bluetooth adapter tersedia
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show()
            return
        }

        // Check apakah Bluetooth enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Semua permission sudah ada, coba connect
            esp32Service.connectToDevice("XX:XX:XX:XX:XX:XX") // Ganti dengan MAC address ESP32 yang benar
        }
    }
}

// Theme Data Classes (sama seperti sebelumnya)
data class AppTheme(
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val backgroundColor: List<Color>,
    val cardColor: Color,
    val textColor: Color,
    val icon: ImageVector
)

val availableThemes = listOf(
    AppTheme(
        name = "Dark Blue",
        primaryColor = Color(0xFFE94560),
        secondaryColor = Color(0xFF4ECDC4),
        accentColor = Color(0xFFFFD93D),
        backgroundColor = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        ),
        cardColor = Color(0xFF1E1E2E),
        textColor = Color.White,
        icon = Icons.Default.DarkMode
    ),
    AppTheme(
        name = "Ocean",
        primaryColor = Color(0xFF00D4AA),
        secondaryColor = Color(0xFF4FC3F7),
        accentColor = Color(0xFF26C6DA),
        backgroundColor = listOf(
            Color(0xFF0D47A1),
            Color(0xFF1565C0),
            Color(0xFF1976D2)
        ),
        cardColor = Color(0xFF1E3A8A),
        textColor = Color.White,
        icon = Icons.Default.Water
    ),
    AppTheme(
        name = "Sunset",
        primaryColor = Color(0xFFFF6B35),
        secondaryColor = Color(0xFFF7931E),
        accentColor = Color(0xFFFFD23F),
        backgroundColor = listOf(
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
            Color(0xFFEF4444)
        ),
        cardColor = Color(0xFF7C2D12),
        textColor = Color.White,
        icon = Icons.Default.WbSunny
    ),
    AppTheme(
        name = "Forest",
        primaryColor = Color(0xFF34D399),
        secondaryColor = Color(0xFF10B981),
        accentColor = Color(0xFF059669),
        backgroundColor = listOf(
            Color(0xFF064E3B),
            Color(0xFF065F46),
            Color(0xFF047857)
        ),
        cardColor = Color(0xFF1F2937),
        textColor = Color.White,
        icon = Icons.Default.Forest
    ),
    AppTheme(
        name = "Purple",
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFFA855F7),
        accentColor = Color(0xFFC084FC),
        backgroundColor = listOf(
            Color(0xFF581C87),
            Color(0xFF6B21A8),
            Color(0xFF7C2D92)
        ),
        cardColor = Color(0xFF312E81),
        textColor = Color.White,
        icon = Icons.Default.ColorLens
    ),
    AppTheme(
        name = "Light",
        primaryColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFF10B981),
        accentColor = Color(0xFFF59E0B),
        backgroundColor = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        ),
        cardColor = Color.White,
        textColor = Color(0xFF1E293B),
        icon = Icons.Default.LightMode
    )
)

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Home")
    object Profile : NavigationItem("profile", Icons.Default.Person, "Profile")
    object Settings : NavigationItem("settings", Icons.Default.Settings, "Settings")
    object History : NavigationItem("history", Icons.Default.History, "History")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHeadbandApp(esp32Service: ESP32BluetoothService) {
    var selectedItem by remember { mutableStateOf(0) }
    var selectedTheme by remember { mutableStateOf(availableThemes[0]) }
    var userName by remember { mutableStateOf("Senna Farras Hazim") }
    var userAge by remember { mutableStateOf("17") }
    var userHeight by remember { mutableStateOf("175") }
    var userWeight by remember { mutableStateOf("70") }

    // Collect data dari ESP32 service
    val sensorData by esp32Service.sensorData.collectAsState()
    val connectionStatus by esp32Service.connectionStatus.collectAsState()
    val errorMessage by esp32Service.errorMessage.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }


    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Profile,
        NavigationItem.Settings,
        NavigationItem.History
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = selectedTheme.cardColor.copy(alpha = 0.95f),
                contentColor = selectedTheme.textColor
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = if (selectedItem == index) selectedTheme.primaryColor else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                color = if (selectedItem == index) selectedTheme.primaryColor else Color.Gray
                            )
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedTheme.primaryColor,
                            selectedTextColor = selectedTheme.primaryColor,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = selectedTheme.primaryColor.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = selectedTheme.backgroundColor
                    )
                )
        ) {
            when (selectedItem) {
                0 -> HomeScreen(
                    isConnected = connectionStatus,
                    heartRate = if (connectionStatus) sensorData.heartRate else -1,
                    oxygenLevel = if (connectionStatus) sensorData.oxygenLevel else -1,
                    isActiveActivity = connectionStatus && sensorData.heartRate > 100,
                    temperature = if (connectionStatus) sensorData.temperature else Float.NaN,
                    stressLevel = if (connectionStatus) sensorData.stressLevel else 0,
                    batteryLevel = if (connectionStatus) sensorData.batteryLevel else -1,
                    theme = selectedTheme
                )
                1 -> ProfileScreen(
                    userName = userName,
                    userAge = userAge,
                    userHeight = userHeight,
                    userWeight = userWeight,
                    onNameChange = { userName = it },
                    onAgeChange = { userAge = it },
                    onHeightChange = { userHeight = it },
                    onWeightChange = { userWeight = it },
                    theme = selectedTheme
                )
                2 -> SettingsScreen(
                    selectedTheme = selectedTheme,
                    onThemeChange = { selectedTheme = it },
                    theme = selectedTheme
                )
                3 -> HistoryScreen(theme = selectedTheme)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isConnected: Boolean,
    heartRate: Int,
    oxygenLevel: Int,
    isActiveActivity: Boolean,
    temperature: Float,
    stressLevel: Int,
    batteryLevel: Int,
    theme: AppTheme
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = theme.primaryColor
                    )
                    Text(
                        "KAVLO Mobile App",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                }
            },
            actions = {
                ConnectionIndicator(isConnected = isConnected, theme = theme)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Connected" else "Disconnected",
                    fontSize = 12.sp,
                    color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeartRateCard(
                        heartRate = if (isConnected) heartRate else -1,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    OxygenCard(
                        oxygenLevel = oxygenLevel,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                ActivityCard(
                    isActive = isActiveActivity,
                    theme = theme,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Temperature",
                        value = "${String.format("%.1f", temperature)}°C",
                        icon = Icons.Default.Thermostat,
                        color = theme.primaryColor,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Stress Level",
                        value = "$stressLevel%",
                        icon = Icons.Default.Psychology,
                        color = theme.secondaryColor,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BatteryCard(
                        batteryLevel = batteryLevel,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    DeviceInfoCard(
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Fungsi-fungsi Composable lainnya tetap sama seperti kode asli
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userAge: String,
    userHeight: String,
    userWeight: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    theme: AppTheme
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                theme.primaryColor.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Text(
                        text = "Smart Headband User",
                        fontSize = 14.sp,
                        color = if (theme.name == "Light") Color.Gray else Color.Gray
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        label = { Text("Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userAge,
                        onValueChange = onAgeChange,
                        label = { Text("Age", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userHeight,
                        onValueChange = onHeightChange,
                        label = { Text("Height (cm)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userWeight,
                        onValueChange = onWeightChange,
                        label = { Text("Weight (kg)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Health Goals",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileGoalItem(
                        title = "Target Heart Rate",
                        value = "120-150 BPM",
                        icon = Icons.Default.Favorite,
                        color = theme.primaryColor,
                        theme = theme
                    )

                    ProfileGoalItem(
                        title = "Daily Activity Goal",
                        value = "60 minutes",
                        icon = Icons.Default.DirectionsRun,
                        color = theme.accentColor,
                        theme = theme
                    )

                    ProfileGoalItem(
                        title = "Stress Management",
                        value = "Keep below 30%",
                        icon = Icons.Default.Psychology,
                        color = theme.secondaryColor,
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileGoalItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    theme: AppTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = theme.textColor
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsScreen(
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    theme: AppTheme
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Theme Selection Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Theme Selection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Text(
                        text = "Choose your preferred theme",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableThemes) { themeOption ->
                            ThemeCard(
                                theme = themeOption,
                                isSelected = selectedTheme == themeOption,
                                onSelect = { onThemeChange(themeOption) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Device Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsItem(
                        title = "Bluetooth Connection",
                        subtitle = "Manage device connection",
                        icon = Icons.Default.Bluetooth,
                        color = Color(0xFF2196F3),
                        theme = theme
                    )

                    SettingsItem(
                        title = "Auto-sync",
                        subtitle = "Sync data automatically",
                        icon = Icons.Default.Sync,
                        color = Color(0xFF4CAF50),
                        theme = theme
                    )

                    SettingsItem(
                        title = "Battery Optimization",
                        subtitle = "Optimize battery usage",
                        icon = Icons.Default.Battery6Bar,
                        color = theme.accentColor,
                        theme = theme
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsItem(
                        title = "Health Alerts",
                        subtitle = "Get notified about health changes",
                        icon = Icons.Default.Notifications,
                        color = theme.primaryColor,
                        theme = theme
                    )

                    SettingsItem(
                        title = "Activity Reminders",
                        subtitle = "Reminders to stay active",
                        icon = Icons.Default.AccessAlarm,
                        color = Color(0xFFFF9800),
                        theme = theme
                    )

                    SettingsItem(
                        title = "Low Battery Warning",
                        subtitle = "Alert when battery is low",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFFF5722),
                        theme = theme
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Privacy & Security",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsItem(
                        title = "Data Privacy",
                        subtitle = "Control your data sharing",
                        icon = Icons.Default.Security,
                        color = Color(0xFF9C27B0),
                        theme = theme
                    )

                    SettingsItem(
                        title = "Export Data",
                        subtitle = "Download your health data",
                        icon = Icons.Default.Download,
                        color = Color(0xFF607D8B),
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp, 120.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                theme.primaryColor.copy(alpha = 0.3f)
            else
                Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, theme.primaryColor)
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Theme preview colors
            Row(
                modifier = Modifier.height(20.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(theme.primaryColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(theme.secondaryColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(theme.accentColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                theme.icon,
                contentDescription = null,
                tint = if (isSelected) theme.primaryColor else theme.textColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = theme.name,
                fontSize = 12.sp,
                color = if (isSelected) theme.primaryColor else theme.textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    theme: AppTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = theme.textColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun HistoryScreen(theme: AppTheme) {
    val historyData = remember {
        listOf(
            HistoryEntry("Today", "Heart Rate: 72 BPM", "12:30 PM", Icons.Default.Favorite, theme.primaryColor),
            HistoryEntry("Today", "SpO2: 98%", "12:00 PM", Icons.Default.Air, theme.secondaryColor),
            HistoryEntry("Today", "Exercise: 30 min", "11:30 AM", Icons.Default.DirectionsRun, theme.accentColor),
            HistoryEntry("Today", "Stress: Low", "10:00 AM", Icons.Default.Psychology, theme.secondaryColor),
            HistoryEntry("Yesterday", "Heart Rate: 75 BPM", "8:00 PM", Icons.Default.Favorite, theme.primaryColor),
            HistoryEntry("Yesterday", "Temperature: 36.8°C", "7:30 PM", Icons.Default.Thermostat, Color(0xFFFF6B6B)),
            HistoryEntry("Yesterday", "Exercise: 45 min", "6:00 PM", Icons.Default.DirectionsRun, theme.accentColor),
            HistoryEntry("2 days ago", "Heart Rate: 68 BPM", "9:00 AM", Icons.Default.Favorite, theme.primaryColor),
            HistoryEntry("2 days ago", "SpO2: 97%", "8:30 AM", Icons.Default.Air, theme.secondaryColor),
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(historyData.groupBy { it.date }.toList()) { (date, entries) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = date,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    entries.forEach { entry ->
                        HistoryItem(entry = entry, theme = theme)
                        if (entry != entries.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

data class HistoryEntry(
    val date: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HistoryItem(entry: HistoryEntry, theme: AppTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(entry.color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                entry.icon,
                contentDescription = null,
                tint = entry.color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entry.description,
                fontSize = 14.sp,
                color = theme.textColor
            )
            Text(
                text = entry.time,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ConnectionIndicator(isConnected: Boolean, theme: AppTheme) {
    val color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5722)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun HeartRateCard(heartRate: Int, theme: AppTheme, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = theme.primaryColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (heartRate >= 0) "$heartRate" else "--",   // <-- tampilkan "--" saat disconnect
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )
            Text(
                text = "BPM",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (heartRate >= 0) { // <-- wave cuma jalan kalau ada data
                HeartRateWave(heartRate = heartRate, theme = theme)
            }
        }
    }
}


@Composable
fun HeartRateWave(heartRate: Int, theme: AppTheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        for (i in 0 until width.toInt() step 4) {
            val x = i.toFloat()
            val y = centerY + sin(phase + x * 0.02f) * 10f
            drawCircle(
                color = theme.primaryColor,
                radius = 1.5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun OxygenCard(oxygenLevel: Int, theme: AppTheme, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Air,
                contentDescription = null,
                tint = theme.secondaryColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (oxygenLevel >= 0) "$oxygenLevel%" else "--",   // <-- tampilkan "--"
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )
            Text(
                text = "SpO2",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (oxygenLevel >= 0) { // <-- jangan tampilkan progress bar saat disconnect
                CircularProgressIndicator(
                    progress = oxygenLevel / 100f,
                    modifier = Modifier.size(40.dp),
                    color = theme.secondaryColor,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun ActivityCard(isActive: Boolean, theme: AppTheme, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isActive) Icons.Default.DirectionsRun else Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = if (isActive) theme.accentColor else theme.secondaryColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isActive) "Active Exercise" else "Resting",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )
                Text(
                    text = if (isActive) "High intensity detected" else "Low activity level",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,    // <-- tetap String
    icon: ImageVector,
    color: Color,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,   // <-- bisa angka atau "--"
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BatteryCard(batteryLevel: Int, theme: AppTheme, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Battery6Bar,
                contentDescription = null,
                tint = when {
                    batteryLevel < 0 -> Color.Gray                 // <-- abu-abu kalau disconnect
                    batteryLevel > 50 -> Color(0xFF4CAF50)
                    batteryLevel > 20 -> theme.accentColor
                    else -> theme.primaryColor
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (batteryLevel >= 0) "$batteryLevel%" else "--",   // <-- tampilkan "--"
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )
            Text(
                text = "Battery",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun DeviceInfoCard(theme: AppTheme, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Headset,
                contentDescription = null,
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SH-001",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )
            Text(
                text = "Smart Headband",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmartHeadbandAppPreview() {
    SmartHeadbandTheme {
    }
}