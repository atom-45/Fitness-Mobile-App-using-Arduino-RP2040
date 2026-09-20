package com.atom.bluetoothfitnessapplication.presentation.activities

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.atom.bluetoothfitnessapplication.R
import com.atom.bluetoothfitnessapplication.data.interfaces.StartStopButtonListener
import com.atom.bluetoothfitnessapplication.di.application.FitnessApplication
import com.atom.bluetoothfitnessapplication.factories.ViewModelFactory
import com.atom.bluetoothfitnessapplication.presentation.screens.MainScreen
import com.atom.bluetoothfitnessapplication.presentation.screens.MainUiState
import com.atom.bluetoothfitnessapplication.presentation.theme.FitnessAppTheme
import com.atom.bluetoothfitnessapplication.presentation.viewmodels.ExerciseViewModel
import com.atom.bluetoothfitnessapplication.services.BluetoothLeService
import com.atom.bluetoothfitnessapplication.utilities.Constants
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity(), StartStopButtonListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000

    private var connected = false
    private var deviceAddress: String? = null
    private var bluetoothLeService: BluetoothLeService? = null

    private val uiState = MainUiState()
    private lateinit var exerciseViewModel: ExerciseViewModel
    private var dateOfExercise: String = ""

    private val CHANNEL_ID = "bluetooth_status_channel"
    private val NOTIFICATION_ID = 101

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) setupBluetooth()
        else Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothLeService?.let {
                it.initialize()
                if (it.isConnected) {
                    connected = true
                    uiState.isBluetoothConnected = true
                    uiState.bluetoothStatus = getString(R.string.bluetooth_connected)
                    uiState.bluetoothIconRes = R.drawable.round_bluetooth_connected_24
                }
                deviceAddress?.let { address -> it.connect(address) }
            }
        }
        override fun onServiceDisconnected(name: ComponentName) { bluetoothLeService = null }
    }

    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val address = result.device.address
            if (address == deviceAddress && connected) return
            deviceAddress = address
            bluetoothLeScanner?.stopScan(this)
            scanning = false
            bluetoothLeService?.connect(deviceAddress!!)
        }
    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val session = exerciseViewModel.activeSession
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    scanning = false
                    uiState.isScanning = false
                    uiState.isBluetoothConnected = true
                    uiState.bluetoothStatus = getString(R.string.bluetooth_connected)
                    uiState.bluetoothIconRes = R.drawable.round_bluetooth_connected_24
                    showConnectedNotification()
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    uiState.isBluetoothConnected = false
                    uiState.bluetoothStatus = getString(R.string.bluetooth_not_connected)
                    uiState.bluetoothIconRes = R.drawable.round_bluetooth_disabled_24
                }
                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
                    val data = intent.getFloatArrayExtra(BluetoothLeService.EXTRA_SENSOR_DATA)
                    data?.let {
                        if (session.isRunning) {
                            exerciseViewModel.saveExerciseData(
                                it.map { v -> v.toString() }.toTypedArray(),
                                dateOfExercise,
                                getString(R.string.push_up),
                                getString(R.string.sit_up),
                                getString(R.string.skipping),
                                getString(R.string.walking),
                                getString(R.string.flap_jack),
                                getString(R.string.weights),
                                getString(R.string.backs),
                                getString(R.string.mt_climbers),
                                getString(R.string.plank)
                            )
                            val accMag = sqrt(it[0].toDouble().pow(2.0) + it[1].toDouble().pow(2.0) + it[2].toDouble().pow(2.0)).toFloat()
                            val gyroMag = sqrt(it[3].toDouble().pow(2.0) + it[4].toDouble().pow(2.0) + it[5].toDouble().pow(2.0)).toFloat()

                            session.magnitudes.add(accMag)
                            session.gyroX.add(it[3])
                            session.gyroY.add(it[4])
                            session.gyroZ.add(it[5])
                            session.gyroMagnitudes.add(gyroMag)
                            
                            // Optimization: Recalculate reps every 5 samples to reduce UI thread load
                            if (session.magnitudes.size % 5 == 0) {
                                val reps = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.countReps(
                                    session.magnitudes, 
                                    session.gyroMagnitudes
                                )
                                session.liveRepCount = reps
                                uiState.liveRepCount = reps
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        (application as FitnessApplication).applicationComponent.inject(this)

        createNotificationChannel()
        exerciseViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseViewModel::class.java]

        val localDateTime = LocalDateTime.now()
        val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
        dateOfExercise = "${localDateTime.dayOfMonth} $month ${localDateTime.year}"

        // Re-sync UI state from the persistent ViewModel session
        val session = exerciseViewModel.activeSession
        uiState.selectedExercise = session.selectedExercise
        uiState.liveRepCount = session.liveRepCount

        // Handle Quick Start from Widget
        intent?.getStringExtra("EXTRA_EXERCISE")?.let { exercise ->
            uiState.selectedExercise = exercise
            exerciseViewModel.activeSession.selectedExercise = exercise
        }

        setContent {
            FitnessAppTheme {
                MainScreen(
                    bluetoothStatus = uiState.bluetoothStatus,
                    bluetoothIconRes = uiState.bluetoothIconRes,
                    timerText = uiState.timerText,
                    exerciseDescription = uiState.exerciseDescription,
                    onDescriptionChange = { uiState.exerciseDescription = it },
                    onSaveDescription = { 
                        exerciseViewModel.saveDescriptionData(uiState.exerciseDescription) {
                            Toast.makeText(this@MainActivity, "Session Notes saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    selectedExercise = uiState.selectedExercise,
                    onExerciseSelect = { 
                        uiState.selectedExercise = it
                        exerciseViewModel.activeSession.selectedExercise = it
                        exerciseViewModel.onStartTimer(uiState)
                    },
                    onStopTimer = { 
                        exerciseViewModel.stopExerciseSession(
                            this@MainActivity,
                            uiState,
                            getString(R.string.push_up),
                            getString(R.string.sit_up),
                            getString(R.string.skipping),
                            getString(R.string.walking),
                            getString(R.string.flap_jack),
                            getString(R.string.weights),
                            getString(R.string.backs),
                            getString(R.string.mt_climbers),
                            getString(R.string.plank)
                        )
                    },
                    onResetTimer = { exerciseViewModel.onResetTimer(uiState) },
                    onPlotGraph = { ex, _, _, isReps -> 
                        exerciseViewModel.fetchTrendData(ex, isReps, uiState, getString(R.string.plank), getString(R.string.skipping), getString(R.string.mt_climbers))
                    },
                    onClearGraph = { uiState.barData = null },
                    exerciseTypes = resources.getStringArray(R.array.exercises_array),
                    initialExerciseType = resources.getStringArray(R.array.exercises_array)[0],
                    initialDate = dateOfExercise,
                    isAccelerometer = uiState.isRepsTrend,
                    onPlotTypeChange = { 
                        uiState.isRepsTrend = it
                        uiState.selectedExercise?.let { ex -> 
                            exerciseViewModel.fetchTrendData(ex, it, uiState, getString(R.string.plank), getString(R.string.skipping), getString(R.string.mt_climbers))
                        }
                    },
                    liveReps = uiState.liveRepCount,
                    currentSummary = uiState.currentWorkoutSummary,
                    pastSummaries = uiState.pastSummaries,
                    isBluetoothConnected = uiState.isBluetoothConnected,
                    isScanning = uiState.isScanning,
                    onReconnect = { setupBluetooth() },
                    barData = uiState.barData,
                    recentActivity = uiState.allRecentSummaries,
                    exerciseStats = uiState.exerciseFrequencyStats,
                    drillDownSummaries = uiState.drillDownSummaries,
                    drillDownExercise = uiState.drillDownExerciseName,
                    onStatsCardClick = { exercise -> exerciseViewModel.fetchDrillDownSummaries(exercise, uiState) },
                    onCloseDrillDown = { 
                        uiState.drillDownSummaries = emptyList()
                        uiState.drillDownExerciseName = null
                    },
                    onDeleteActivity = { summary -> 
                        exerciseViewModel.deleteWorkoutSummary(summary, uiState)
                    }
                )
            }
        }

        exerciseViewModel.startTimerLoop(uiState)
        exerciseViewModel.fetchAllRecentSummaries(uiState)
        exerciseViewModel.fetchExerciseStats(uiState)

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val list = mutableListOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) list.add(Manifest.permission.POST_NOTIFICATIONS)
            list.toTypedArray()
        } else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissionLauncher.launch(missing.toTypedArray())
        else setupBluetooth()
    }

    private fun createNotificationChannel() {
        val channel = android.app.NotificationChannel(CHANNEL_ID, "Workout Tracker", android.app.NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun showConnectedNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.tracker)
            .setContentTitle("Sensor Connected").setAutoCancel(true)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    private fun setupBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter ?: return
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        if (bluetoothAdapter?.isEnabled == false) startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        scanBLEDevice()
    }

    private fun scanBLEDevice() {
        if (scanning) return
        uiState.isScanning = true
        val filter = android.bluetooth.le.ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(Constants.RP2040_SERVICE_UUID)).build()
        val settings = android.bluetooth.le.ScanSettings.Builder().setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        handler.postDelayed({ 
            if (scanning) { 
                scanning = false
                uiState.isScanning = false
                bluetoothLeScanner?.stopScan(bleScanCallback) 
            } 
        }, SCAN_PERIOD)
        scanning = true
        bluetoothLeScanner?.startScan(listOf(filter), settings, bleScanCallback)
    }

    override fun startStopButtonPressed(buttonStatus: String) {
        bluetoothLeService?.writeCharacteristicToBLEDevice(buttonStatus)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("EXTRA_EXERCISE")?.let { exercise ->
            uiState.selectedExercise = exercise
            exerciseViewModel.activeSession.selectedExercise = exercise
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) registerReceiver(gattUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        else registerReceiver(gattUpdateReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
