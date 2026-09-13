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
import com.atom.bluetoothfitnessapplication.data.models.*
import com.atom.bluetoothfitnessapplication.di.application.FitnessApplication
import com.atom.bluetoothfitnessapplication.factories.ViewModelFactory
import com.atom.bluetoothfitnessapplication.presentation.screens.MainScreen
import com.atom.bluetoothfitnessapplication.presentation.screens.MainUiState
import com.atom.bluetoothfitnessapplication.presentation.theme.FitnessAppTheme
import com.atom.bluetoothfitnessapplication.presentation.viewmodels.ExerciseViewModel
import com.atom.bluetoothfitnessapplication.services.BluetoothLeService
import com.atom.bluetoothfitnessapplication.utilities.Constants
import com.atom.bluetoothfitnessapplication.utilities.WidgetHelper
import com.github.mikephil.charting.data.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDateTime
import java.time.LocalTime
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
    private val compositeDisposable = CompositeDisposable()
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
                        saveExerciseData(it.map { v -> v.toString() }.toTypedArray())
                        if (session.isRunning) {
                            val accMag = sqrt(it[0].toDouble().pow(2.0) + it[1].toDouble().pow(2.0) + it[2].toDouble().pow(2.0)).toFloat()
                            val gyroMag = sqrt(it[3].toDouble().pow(2.0) + it[4].toDouble().pow(2.0) + it[5].toDouble().pow(2.0)).toFloat()

                            session.magnitudes.add(accMag)
                            session.gyroX.add(it[3])
                            session.gyroY.add(it[4])
                            session.gyroZ.add(it[5])
                            session.gyroMagnitudes.add(gyroMag)
                            
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
                    onSaveDescription = { saveDescriptionData(uiState.exerciseDescription) },
                    selectedExercise = uiState.selectedExercise,
                    onExerciseSelect = { 
                        uiState.selectedExercise = it
                        exerciseViewModel.activeSession.selectedExercise = it
                    },
                    onStopTimer = { stopExerciseSession() },
                    onResetTimer = { onResetTimer() },
                    onPlotGraph = { ex, _, _, isReps -> fetchTrendData(ex, isReps) },
                    onClearGraph = { uiState.barData = null },
                    exerciseTypes = resources.getStringArray(R.array.exercises_array),
                    initialExerciseType = resources.getStringArray(R.array.exercises_array)[0],
                    initialDate = dateOfExercise,
                    isAccelerometer = uiState.isRepsTrend,
                    onPlotTypeChange = { 
                        uiState.isRepsTrend = it
                        uiState.selectedExercise?.let { ex -> fetchTrendData(ex, it) }
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
                    onStatsCardClick = { exercise -> fetchDrillDownSummaries(exercise) },
                    onCloseDrillDown = { 
                        uiState.drillDownSummaries = emptyList()
                        uiState.drillDownExerciseName = null
                    }
                )
            }
        }

        runTimer()
        fetchAllRecentSummaries()
        fetchExerciseStats()

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
            .setSmallIcon(R.drawable.round_bluetooth_connected_24)
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

    private fun fetchTrendData(exerciseType: String, isPrimaryMetric: Boolean) {
        val disposable = exerciseViewModel.getPastSummaries(exerciseType, 10)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ summaries ->
                if (summaries.isEmpty()) { uiState.barData = null; return@subscribe }
                val isPlank = exerciseType == getString(R.string.plank)
                val isCardio = exerciseType == getString(R.string.skipping) || exerciseType == getString(R.string.mt_climbers)
                val entries = summaries.reversed().mapIndexed { i, s ->
                    val value = when {
                        isPlank -> if (isPrimaryMetric) s.duration.toFloat() else s.stabilityScore
                        isCardio -> if (isPrimaryMetric) (if (s.cadence > 0) 1f/s.cadence else 0f) else s.repCount.toFloat()
                        else -> if (isPrimaryMetric) s.repCount.toFloat() else s.maxPower
                    }
                    BarEntry(i.toFloat(), value)
                }
                val label = when {
                    isPlank -> if (isPrimaryMetric) "Hold Time (s)" else "Stability (%)"
                    isCardio -> if (isPrimaryMetric) "Cadence (reps/s)" else "Total Reps"
                    else -> if (isPrimaryMetric) "Repetitions" else "Peak Power (G)"
                }
                val dataSet = BarDataSet(entries, label).apply {
                    color = android.graphics.Color.parseColor("#00C853")
                    valueTextColor = android.graphics.Color.GRAY
                    setDrawValues(true)
                }
                uiState.barData = BarData(dataSet).apply { barWidth = 0.6f }
            }, { Log.e(TAG, "Trend Error", it) })
        compositeDisposable.add(disposable)
    }

    private fun saveExerciseData(exerciseData: Array<String>) {
        val selected = exerciseViewModel.activeSession.selectedExercise ?: return
        onStartTimer()
        val time = LocalTime.now().toString()
        val disposable = when (selected) {
            getString(R.string.push_up) -> exerciseViewModel.insertPushUp(PushUp(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.sit_up) -> exerciseViewModel.insertSitUps(SitUps(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.skipping) -> exerciseViewModel.insertSkipping(Skipping(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.walking) -> exerciseViewModel.insertWalking(Walking(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.flap_jack) -> exerciseViewModel.insertFlapJacks(Flapjacks(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.weights) -> exerciseViewModel.insertWeights(Weights(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.backs) -> exerciseViewModel.insertBacks(Backs(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.mt_climbers) -> exerciseViewModel.insertMountainClimbers(MountainClimbers(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            getString(R.string.plank) -> exerciseViewModel.insertPlank(Plank(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise))
            else -> null
        }?.subscribeOn(Schedulers.io())?.subscribe()
        disposable?.let { compositeDisposable.add(it) }
    }

    private fun saveDescriptionData(description: String) {
        if (description.isEmpty()) return
        val disposable = exerciseViewModel.insertExerciseDescription(ExerciseDescription(LocalDateTime.now().toString(), description))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show() }, {})
        compositeDisposable.add(disposable)
    }

    private fun fetchAllRecentSummaries() {
        val disposable = exerciseViewModel.getAllRecentSummaries(20)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ summaries -> uiState.allRecentSummaries = summaries }, {})
        compositeDisposable.add(disposable)
    }

    private fun fetchExerciseStats() {
        val disposable = exerciseViewModel.getExerciseFrequencyStats()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ stats -> uiState.exerciseFrequencyStats = stats }, {})
        compositeDisposable.add(disposable)
    }

    private fun fetchDrillDownSummaries(exerciseType: String) {
        val disposable = exerciseViewModel.getPastSummaries(exerciseType, 6)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ summaries ->
                uiState.drillDownSummaries = summaries
                uiState.drillDownExerciseName = exerciseType
            }, { Log.e(TAG, "Error fetching drill down", it) })
        compositeDisposable.add(disposable)
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

    private fun stopExerciseSession() {
        val session = exerciseViewModel.activeSession
        val selected = session.selectedExercise ?: return
        
        // Immediately stop tracking and clear exercise to prevent sensor data from restarting timer
        session.isRunning = false
        session.selectedExercise = null
        uiState.selectedExercise = null
        uiState.liveRepCount = 0
        
        // Immediate UI feedback for the timer
        updateTimerText(session.seconds)
        
        if (session.magnitudes.isNotEmpty()) {
            val reps = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.countReps(
                session.magnitudes,
                session.gyroMagnitudes
            )
            val mean = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.calculateMean(session.magnitudes)
            val max = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.calculateMax(session.magnitudes)
            val stdDev = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.calculateStdDev(session.magnitudes, mean)
            val cadence = if (reps > 0) session.seconds.toFloat() / reps else 0f
            val stability = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.calculateStabilityScore(session.gyroMagnitudes)
            val symmetry = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.calculateSymmetryScore(session.gyroX, session.gyroY, session.gyroZ)

            val summary = WorkoutSummary(selected, reps, max, mean, stdDev, cadence, session.seconds.toLong(), LocalDateTime.now().toString(), stability, symmetry)
            
            if (selected == getString(R.string.sit_up)) {
                summary.rangeOfMotion = com.atom.bluetoothfitnessapplication.utilities.AnalyzerUtils.calculateRangeOfMotion(session.gyroMagnitudes, reps)
            }

            val disposable = exerciseViewModel.getPastSummaries(selected, 6)
                .flatMapCompletable { past ->
                    uiState.pastSummaries = past
                    uiState.currentWorkoutSummary = summary
                    exerciseViewModel.insertWorkoutSummary(summary)
                }
                .andThen(exerciseViewModel.getAllRecentSummaries(20))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ recent -> 
                    uiState.allRecentSummaries = recent
                    fetchExerciseStats() // Refresh counts
                    
                    // Save for Widget
                    val value = when {
                        summary.exerciseType == getString(R.string.plank) -> summary.duration.toFloat()
                        summary.exerciseType == getString(R.string.skipping) || summary.exerciseType == getString(R.string.mt_climbers) -> if (summary.cadence > 0) 1f/summary.cadence else 0f
                        else -> summary.repCount.toFloat()
                    }
                    val unit = when {
                        summary.exerciseType == getString(R.string.plank) -> "sec"
                        summary.exerciseType == getString(R.string.skipping) || summary.exerciseType == getString(R.string.mt_climbers) -> "reps/s"
                        else -> "reps"
                    }
                    val secondary = if (summary.exerciseType == getString(R.string.plank)) 
                        String.format(Locale.getDefault(), "%.0f%% Stab", summary.stabilityScore)
                        else String.format(Locale.getDefault(), "%.1fG Max", summary.maxPower)

                    WidgetHelper.saveLastExercise(this@MainActivity, summary.exerciseType, value, unit, secondary)

                    session.reset()
                }, { Log.e(TAG, "Finalize Error", it) })
            compositeDisposable.add(disposable)
        } else {
            session.reset()
        }
    }

    private fun onStartTimer() { 
        if (!exerciseViewModel.activeSession.isRunning) {
            exerciseViewModel.activeSession.reset()
            uiState.currentWorkoutSummary = null
        }
        exerciseViewModel.activeSession.isRunning = true 
    }

    private fun onResetTimer() {
        exerciseViewModel.activeSession.reset()
        uiState.selectedExercise = null
        uiState.liveRepCount = 0
        updateTimerText(0)
    }

    private fun updateTimerText(totalSeconds: Int) {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        uiState.timerText = String.format(Locale.getDefault(), "%02d : %02d : %02d", hours, minutes, secs)
    }

    private fun runTimer() {
        handler.post(object : Runnable {
            override fun run() {
                val session = exerciseViewModel.activeSession
                if (session.isRunning) {
                    session.incrementSeconds()
                    updateTimerText(session.seconds)
                }
                handler.postDelayed(this, 1000)
            }
        })
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
        compositeDisposable.clear()
        unbindService(serviceConnection)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
