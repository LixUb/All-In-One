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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kavlo.sft.mobile.ui.theme.SmartHeadbandTheme
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos
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
            esp32Service.connectToDevice("XX:XX:XX:XX:XX:XX")
        } else {
            Toast.makeText(this, "Bluetooth permissions required for device connection", Toast.LENGTH_LONG).show()
        }
    }

    // Bluetooth enable launcher
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
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

        checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        esp32Service.cleanup()
    }

    private fun checkAndRequestPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show()
            return
        }

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
            esp32Service.connectToDevice("XX:XX:XX:XX:XX:XX")
        }
    }
}

// Enhanced Theme Data Classes with glassmorphism support
data class AppTheme(
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val backgroundColor: List<Color>,
    val cardColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val textSecondary: Color,
    val icon: ImageVector,
    val isLight: Boolean = false
)

val availableThemes = listOf(
    AppTheme(
        name = "Midnight Pro",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF10B981),
        backgroundColor = listOf(
            Color(0xFF0F0F23),
            Color(0xFF1A1A2E),
            Color(0xFF16213E)
        ),
        cardColor = Color(0xFF1E1E2E),
        surfaceColor = Color(0xFF2A2A3E),
        textColor = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        icon = Icons.Default.DarkMode
    ),
    AppTheme(
        name = "Ocean Depth",
        primaryColor = Color(0xFF0EA5E9),
        secondaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF8B5CF6),
        backgroundColor = listOf(
            Color(0xFF0C4A6E),
            Color(0xFF075985),
            Color(0xFF0369A1)
        ),
        cardColor = Color(0xFF1E3A5F),
        surfaceColor = Color(0xFF2A4A6F),
        textColor = Color.White,
        textSecondary = Color(0xFF94A3B8),
        icon = Icons.Default.Water
    ),
    AppTheme(
        name = "Sunset Glow",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFF59E0B),
        accentColor = Color(0xFFEC4899),
        backgroundColor = listOf(
            Color(0xFF7C2D12),
            Color(0xFF991B1B),
            Color(0xFFBE123C)
        ),
        cardColor = Color(0xFF7C2D12),
        surfaceColor = Color(0xFF8C3D22),
        textColor = Color.White,
        textSecondary = Color(0xFFFEF3C7),
        icon = Icons.Default.WbSunny
    ),
    AppTheme(
        name = "Forest Deep",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669),
        accentColor = Color(0xFF34D399),
        backgroundColor = listOf(
            Color(0xFF064E3B),
            Color(0xFF065F46),
            Color(0xFF047857)
        ),
        cardColor = Color(0xFF1F2937),
        surfaceColor = Color(0xFF2F3947),
        textColor = Color.White,
        textSecondary = Color(0xFFD1FAE5),
        icon = Icons.Default.Forest
    ),
    AppTheme(
        name = "Royal Purple",
        primaryColor = Color(0xFF8B5CF6),
        secondaryColor = Color(0xFFA855F7),
        accentColor = Color(0xFFC084FC),
        backgroundColor = listOf(
            Color(0xFF581C87),
            Color(0xFF6B21A8),
            Color(0xFF7C2D92)
        ),
        cardColor = Color(0xFF2E1065),
        surfaceColor = Color(0xFF3E2075),
        textColor = Color.White,
        textSecondary = Color(0xFFDDD6FE),
        icon = Icons.Default.ColorLens
    ),
    AppTheme(
        name = "Pure Light",
        primaryColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF8B5CF6),
        backgroundColor = listOf(
            Color(0xFFFAFAFA),
            Color(0xFFF5F5F5),
            Color(0xFFE5E5E5)
        ),
        cardColor = Color(0xFFFFFFFF),
        surfaceColor = Color(0xFFF8F9FA),
        textColor = Color(0xFF1E293B),
        textSecondary = Color(0xFF64748B),
        icon = Icons.Default.LightMode,
        isLight = true
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
            ModernNavigationBar(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                theme = selectedTheme
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = selectedTheme.backgroundColor,
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        ) {
            when (selectedItem) {
                0 -> ModernHomeScreen(
                    isConnected = connectionStatus,
                    heartRate = if (connectionStatus) sensorData.heartRate else -1,
                    oxygenLevel = if (connectionStatus) sensorData.oxygenLevel else -1,
                    isActiveActivity = connectionStatus && sensorData.heartRate > 100,
                    temperature = if (connectionStatus) sensorData.temperature else Float.NaN,
                    stressLevel = if (connectionStatus) sensorData.stressLevel else 0,
                    batteryLevel = if (connectionStatus) sensorData.batteryLevel else -1,
                    theme = selectedTheme,
                    modifier = Modifier.padding(innerPadding)
                )
                1 -> ModernProfileScreen(
                    userName = userName,
                    userAge = userAge,
                    userHeight = userHeight,
                    userWeight = userWeight,
                    onNameChange = { userName = it },
                    onAgeChange = { userAge = it },
                    onHeightChange = { userHeight = it },
                    onWeightChange = { userWeight = it },
                    theme = selectedTheme,
                    modifier = Modifier.padding(innerPadding)
                )
                2 -> ModernSettingsScreen(
                    selectedTheme = selectedTheme,
                    onThemeChange = { selectedTheme = it },
                    theme = selectedTheme,
                    modifier = Modifier.padding(innerPadding)
                )
                3 -> ModernHistoryScreen(
                    theme = selectedTheme,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun ModernNavigationBar(
    items: List<NavigationItem>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    theme: AppTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                NavigationItem(
                    icon = item.icon,
                    title = item.title,
                    isSelected = selectedItem == index,
                    onClick = { onItemSelected(index) },
                    theme = theme
                )
            }
        }
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .scale(animatedScale)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isSelected) theme.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = if (isSelected) theme.primaryColor else theme.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(alpha = animatedAlpha)
            )
        }

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = theme.primaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHomeScreen(
    isConnected: Boolean,
    heartRate: Int,
    oxygenLevel: Int,
    isActiveActivity: Boolean,
    temperature: Float,
    stressLevel: Int,
    batteryLevel: Int,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Modern Header
        ModernHeader(
            title = "KAVLO",
            subtitle = "Smart Health Monitoring",
            isConnected = isConnected,
            theme = theme
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernHeartRateCard(
                        heartRate = heartRate,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                    ModernOxygenCard(
                        oxygenLevel = oxygenLevel,
                        theme = theme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Activity Status Card
            item {
                ModernActivityCard(
                    isActive = isActiveActivity,
                    heartRate = heartRate,
                    theme = theme
                )
            }

            // Metrics Grid
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernMetricCard(
                            title = "Temperature",
                            value = if (temperature.isNaN()) "--" else "${String.format("%.1f", temperature)}°C",
                            icon = Icons.Default.Thermostat,
                            color = Color(0xFFEF4444),
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        ModernMetricCard(
                            title = "Stress Level",
                            value = if (stressLevel > 0) "$stressLevel%" else "--",
                            icon = Icons.Default.Psychology,
                            color = theme.accentColor,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernBatteryCard(
                            batteryLevel = batteryLevel,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        ModernDeviceCard(
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHeader(
    title: String,
    subtitle: String,
    isConnected: Boolean,
    theme: AppTheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            theme.primaryColor.copy(alpha = 0.1f),
                            theme.secondaryColor.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = theme.textSecondary
                    )
                }

                ModernConnectionStatus(
                    isConnected = isConnected,
                    theme = theme
                )
            }
        }
    }
}

@Composable
fun ModernConnectionStatus(
    isConnected: Boolean,
    theme: AppTheme
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(if (isConnected) pulseScale else 1f)
                .background(
                    if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                    CircleShape
                )
        )
        Text(
            text = if (isConnected) "Connected" else "Disconnected",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
        )
    }
}

@Composable
fun ModernHeartRateCard(
    heartRate: Int,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                theme.primaryColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            radius = 150f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = theme.primaryColor,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (heartRate >= 0) heartRate.toString() else "--",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )

                Text(
                    text = "BPM",
                    fontSize = 14.sp,
                    color = theme.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                if (heartRate >= 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernHeartWave(theme = theme)
                }
            }
        }
    }
}

@Composable
fun ModernHeartWave(theme: AppTheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartwave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        val path = Path()
        for (i in 0 until width.toInt() step 2) {
            val x = i.toFloat()
            val normalizedX = x / width
            val y = centerY + sin(phase + normalizedX * 4 * Math.PI) * (height * 0.3f)

            if (i == 0) path.moveTo(x, y.toFloat())
            else path.lineTo(x, y.toFloat())
        }

        drawPath(
            path = path,
            color = theme.primaryColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ModernOxygenCard(
    oxygenLevel: Int,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                theme.secondaryColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            radius = 150f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Air,
                    contentDescription = null,
                    tint = theme.secondaryColor,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (oxygenLevel >= 0) "$oxygenLevel%" else "--",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )

                Text(
                    text = "SpO2",
                    fontSize = 14.sp,
                    color = theme.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                if (oxygenLevel >= 0) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = oxygenLevel / 100f,
                            modifier = Modifier.size(40.dp),
                            color = theme.secondaryColor,
                            strokeWidth = 3.dp,
                            trackColor = theme.secondaryColor.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernActivityCard(
    isActive: Boolean,
    heartRate: Int,
    theme: AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isActive) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                theme.accentColor.copy(alpha = 0.2f),
                                theme.primaryColor.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                theme.textSecondary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            if (isActive) theme.accentColor.copy(alpha = 0.2f) else theme.textSecondary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isActive) Icons.Default.DirectionsRun else Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = if (isActive) theme.accentColor else theme.textSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isActive) "Active Exercise" else "Resting State",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    Text(
                        text = when {
                            heartRate > 120 -> "High intensity workout detected"
                            heartRate > 100 -> "Moderate activity level"
                            heartRate > 0 -> "Light activity"
                            else -> "No activity data"
                        },
                        fontSize = 14.sp,
                        color = theme.textSecondary
                    )
                }

                if (heartRate > 0) {
                    Text(
                        text = "$heartRate",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) theme.accentColor else theme.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = theme.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ModernBatteryCard(
    batteryLevel: Int,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        batteryLevel < 0 -> theme.textSecondary
        batteryLevel > 50 -> Color(0xFF10B981)
        batteryLevel > 20 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                batteryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Battery6Bar,
                    contentDescription = null,
                    tint = batteryColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (batteryLevel >= 0) "$batteryLevel%" else "--",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textColor
                )

                Text(
                    text = "Battery",
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )
            }
        }
    }
}

@Composable
fun ModernDeviceCard(
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Headset,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
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
                    text = "Headband",
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernProfileScreen(
    userName: String,
    userAge: String,
    userHeight: String,
    userWeight: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Profile",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    theme.primaryColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                radius = 300f
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            theme.primaryColor.copy(alpha = 0.3f),
                                            theme.primaryColor.copy(alpha = 0.1f)
                                        )
                                    ),
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
                            color = theme.textSecondary
                        )
                    }
                }
            }
        }

        // Personal Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ModernTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        label = "Name",
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ModernTextField(
                        value = userAge,
                        onValueChange = onAgeChange,
                        label = "Age",
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ModernTextField(
                        value = userHeight,
                        onValueChange = onHeightChange,
                        label = "Height (cm)",
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ModernTextField(
                        value = userWeight,
                        onValueChange = onWeightChange,
                        label = "Weight (kg)",
                        theme = theme
                    )
                }
            }
        }

        // Health Goals Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.cardColor.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Health Goals",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ModernGoalItem(
                        title = "Target Heart Rate",
                        value = "120-150 BPM",
                        icon = Icons.Default.Favorite,
                        color = theme.primaryColor,
                        theme = theme
                    )

                    ModernGoalItem(
                        title = "Daily Activity Goal",
                        value = "60 minutes",
                        icon = Icons.Default.DirectionsRun,
                        color = theme.accentColor,
                        theme = theme
                    )

                    ModernGoalItem(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    theme: AppTheme
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = theme.textSecondary) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = theme.textColor,
            unfocusedTextColor = theme.textColor,
            focusedBorderColor = theme.primaryColor,
            unfocusedBorderColor = theme.textSecondary.copy(alpha = 0.5f),
            cursorColor = theme.primaryColor
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ModernGoalItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    theme: AppTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textColor
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = theme.textSecondary
            )
        }
    }
}

@Composable
fun ModernSettingsScreen(
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 32.sp,
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
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Theme Selection",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                    Text(
                        text = "Choose your preferred visual style",
                        fontSize = 14.sp,
                        color = theme.textSecondary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(availableThemes) { themeOption ->
                            ModernThemeCard(
                                theme = themeOption,
                                isSelected = selectedTheme == themeOption,
                                onSelect = { onThemeChange(themeOption) }
                            )
                        }
                    }
                }
            }
        }

        // Device Settings
        item {
            SettingsSection(
                title = "Device Settings",
                items = listOf(
                    SettingsItemData(
                        title = "Bluetooth Connection",
                        subtitle = "Manage device pairing",
                        icon = Icons.Default.Bluetooth,
                        color = Color(0xFF3B82F6)
                    ),
                    SettingsItemData(
                        title = "Auto-sync Data",
                        subtitle = "Automatic data synchronization",
                        icon = Icons.Default.Sync,
                        color = Color(0xFF10B981)
                    ),
                    SettingsItemData(
                        title = "Battery Optimization",
                        subtitle = "Optimize power consumption",
                        icon = Icons.Default.Battery6Bar,
                        color = theme.accentColor
                    )
                ),
                theme = theme
            )
        }

        // Notifications
        item {
            SettingsSection(
                title = "Notifications",
                items = listOf(
                    SettingsItemData(
                        title = "Health Alerts",
                        subtitle = "Critical health notifications",
                        icon = Icons.Default.Notifications,
                        color = theme.primaryColor
                    ),
                    SettingsItemData(
                        title = "Activity Reminders",
                        subtitle = "Movement and exercise reminders",
                        icon = Icons.Default.AccessAlarm,
                        color = Color(0xFFF59E0B)
                    ),
                    SettingsItemData(
                        title = "Low Battery Warning",
                        subtitle = "Device battery alerts",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    )
                ),
                theme = theme
            )
        }

        // Privacy & Security
        item {
            SettingsSection(
                title = "Privacy & Security",
                items = listOf(
                    SettingsItemData(
                        title = "Data Privacy",
                        subtitle = "Control your data sharing",
                        icon = Icons.Default.Security,
                        color = Color(0xFF8B5CF6)
                    ),
                    SettingsItemData(
                        title = "Export Data",
                        subtitle = "Download your health data",
                        icon = Icons.Default.Download,
                        color = Color(0xFF6B7280)
                    )
                ),
                theme = theme
            )
        }
    }
}

data class SettingsItemData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItemData>,
    theme: AppTheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            items.forEach { item ->
                ModernSettingsItem(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    color = item.color,
                    theme = theme
                )
            }
        }
    }
}

@Composable
fun ModernSettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    theme: AppTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textColor
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = theme.textSecondary
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = theme.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ModernThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .size(110.dp, 130.dp)
            .scale(animatedScale)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                theme.primaryColor.copy(alpha = 0.2f)
            else
                theme.cardColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, theme.primaryColor)
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Theme preview colors
            Row(
                modifier = Modifier.height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(theme.primaryColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(theme.secondaryColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(theme.accentColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Icon(
                theme.icon,
                contentDescription = null,
                tint = if (isSelected) theme.primaryColor else theme.textColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.name,
                fontSize = 13.sp,
                color = if (isSelected) theme.primaryColor else theme.textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernHistoryScreen(
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    val historyData = remember {
        listOf(
            HistoryEntry("Today", "Heart Rate: 72 BPM", "12:30 PM", Icons.Default.Favorite, theme.primaryColor),
            HistoryEntry("Today", "SpO2: 98%", "12:00 PM", Icons.Default.Air, theme.secondaryColor),
            HistoryEntry("Today", "Exercise: 30 min", "11:30 AM", Icons.Default.DirectionsRun, theme.accentColor),
            HistoryEntry("Today", "Stress: Low", "10:00 AM", Icons.Default.Psychology, theme.secondaryColor),
            HistoryEntry("Yesterday", "Heart Rate: 92 BPM", "13:30 PM", Icons.Default.Favorite, theme.primaryColor),
            HistoryEntry("Yesterday", "SpO2: 98%", "12:00 PM", Icons.Default.Air, theme.secondaryColor),
            HistoryEntry("Yesterday", "Exercise: 30 min", "11:30 AM", Icons.Default.DirectionsRun, theme.accentColor),
            HistoryEntry("Yesterday", "Stress: Low", "10:00 AM", Icons.Default.Psychology, theme.secondaryColor),
            HistoryEntry("2 Days Ago", "Heart Rate: 86 BPM", "11:30 PM", Icons.Default.Favorite, theme.primaryColor),
            HistoryEntry("2 Days Ago", "SpO2: 98%", "12:00 PM", Icons.Default.Air, theme.secondaryColor),
            HistoryEntry("2 Days Ago", "Exercise: 30 min", "11:30 AM", Icons.Default.DirectionsRun, theme.accentColor),
            HistoryEntry("2 Days Ago", "Stress: Low", "10:00 AM", Icons.Default.Psychology, theme.secondaryColor),
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health History",
                fontSize = 32.sp,
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
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = date,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    entries.forEach { entry ->
                        ModernHistoryItem(entry = entry, theme = theme)
                        if (entry != entries.last()) {
                            Spacer(modifier = Modifier.height(12.dp))
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
fun ModernHistoryItem(entry: HistoryEntry, theme: AppTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(entry.color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                entry.icon,
                contentDescription = null,
                tint = entry.color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entry.description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textColor
            )
            Text(
                text = entry.time,
                fontSize = 14.sp,
                color = theme.textSecondary
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