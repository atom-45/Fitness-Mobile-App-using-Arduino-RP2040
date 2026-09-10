package com.atom.bluetoothfitnessapplication.presentation.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.atom.bluetoothfitnessapplication.R
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary
import com.github.mikephil.charting.data.BarData

class MainUiState {
    var bluetoothStatus by mutableStateOf("Not Connected")
    var bluetoothIconRes by mutableStateOf(R.drawable.baseline_bluetooth_searching_24)
    var timerText by mutableStateOf("00 : 00 : 00")
    var exerciseDescription by mutableStateOf("")
    var selectedExercise by mutableStateOf<String?>(null)
    var barData by mutableStateOf<BarData?>(null)
    var isRepsTrend by mutableStateOf(true)

    var isBluetoothConnected by mutableStateOf(false)

    // New analysis state
    var liveRepCount by mutableIntStateOf(0)
    var currentWorkoutSummary by mutableStateOf<WorkoutSummary?>(null)
    var pastSummaries by mutableStateOf<List<WorkoutSummary>>(emptyList())
    var allRecentSummaries by mutableStateOf<List<WorkoutSummary>>(emptyList())
}
