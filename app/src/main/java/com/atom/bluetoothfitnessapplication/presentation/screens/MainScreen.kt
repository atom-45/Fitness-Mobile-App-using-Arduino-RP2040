package com.atom.bluetoothfitnessapplication.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.atom.bluetoothfitnessapplication.R
import com.atom.bluetoothfitnessapplication.data.models.ExerciseStats
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary
import com.atom.bluetoothfitnessapplication.presentation.theme.*
import com.atom.bluetoothfitnessapplication.dsp.DynamicAnalyzerUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    bluetoothStatus: String,
    bluetoothIconRes: Int,
    timerText: String,
    exerciseDescription: String,
    onDescriptionChange: (String) -> Unit,
    onSaveDescription: () -> Unit,
    selectedExercise: String?,
    onExerciseSelect: (String) -> Unit,
    onStopTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onPlotGraph: (String, String, String, Boolean) -> Unit,
    onClearGraph: () -> Unit,
    exerciseTypes: Array<String>,
    initialExerciseType: String,
    initialDate: String,
    isAccelerometer: Boolean,
    onPlotTypeChange: (Boolean) -> Unit,
    liveReps: Int,
    currentSummary: WorkoutSummary?,
    pastSummaries: List<WorkoutSummary>,
    isBluetoothConnected: Boolean,
    isScanning: Boolean,
    onReconnect: () -> Unit,
    barData: BarData?,
    recentActivity: List<WorkoutSummary>,
    exerciseStats: List<ExerciseStats>,
    drillDownSummaries: List<WorkoutSummary> = emptyList(),
    drillDownExercise: String? = null,
    onStatsCardClick: (String) -> Unit = {},
    onCloseDrillDown: () -> Unit = {},
    onDeleteActivity: (WorkoutSummary) -> Unit = {},
    onTimelineItemClick: (WorkoutSummary) -> Unit = {},
    onDismissSummary: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fitness Tracker",
                        fontFamily = Marvel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    if (!isBluetoothConnected && !isScanning) {
                        IconButton(onClick = onReconnect) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reconnect",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    BluetoothStatusIndicator(status = bluetoothStatus, iconRes = bluetoothIconRes)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isScanning,
            onRefresh = onReconnect,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            indicator = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MintGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    } else {
                        val fraction = pullToRefreshState.distanceFraction
                        if (fraction > 0.01f) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = fraction.coerceIn(0f, 1f)
                                        rotationZ = fraction * 180f
                                        translationY = fraction * 100f // Use translation instead of padding
                                    },
                                tint = MintGreen
                            )
                        }
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    WelcomeSection()
                }

            // Moved Multi-view pager here (Above Stopwatch, under text)
            item {
                HistorySection(
                    recentActivity = recentActivity,
                    exerciseStats = exerciseStats,
                    drillDownSummaries = drillDownSummaries,
                    drillDownExercise = drillDownExercise,
                    onStatsCardClick = onStatsCardClick,
                    onCloseDrillDown = onCloseDrillDown,
                    onDeleteActivity = onDeleteActivity,
                    onTimelineItemClick = onTimelineItemClick
                )
            }

            item {
                ActiveSessionCard(
                    timerText = timerText,
                    selectedExercise = selectedExercise,
                    onStop = onStopTimer,
                    onReset = onResetTimer,
                    liveReps = liveReps,
                    isConnected = isBluetoothConnected
                )
            }

            if (currentSummary != null) {
                item {
                    WorkoutSummarySection(
                        summary = currentSummary, 
                        past = pastSummaries,
                        onDismiss = onDismissSummary
                    )
                }
            }

            item {
                ExerciseSelectorSection(
                    selectedExercise = selectedExercise,
                    onExerciseSelect = onExerciseSelect,
                    description = exerciseDescription,
                    onDescriptionChange = onDescriptionChange,
                    onSaveDescription = onSaveDescription
                )
            }

            item {
                InsightsSection(
                    barData = barData,
                    onPlotGraph = onPlotGraph,
                    onClearGraph = onClearGraph,
                    exerciseTypes = exerciseTypes,
                    initialExerciseType = initialExerciseType,
                    initialDate = initialDate,
                    isRepsTrend = isAccelerometer,
                    onTrendTypeChange = onPlotTypeChange
                )
            }
        }
    }
}
}

@Composable
fun WelcomeSection() {
    Column {
        Text(
            text = "Keep Pushing!",
            fontFamily = Marvel,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        DailyMotivationBanner()
    }
}

@Composable
fun DailyMotivationBanner() {
    val affirmations = listOf(
        R.string.i_am_attractive, R.string.i_am_lovable, R.string.i_am_brave,
        R.string.i_can_do_this, R.string.i_am_confident, R.string.i_am_a_king,
        R.string.stay_humble_and_grateful, R.string.the_vision_is_bigger_than_my_current_problems,
        R.string.delay_gratification, R.string.i_am_handsome
    )
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % affirmations.size
        }
    }

    AnimatedContent(
        targetState = affirmations[currentIndex],
        transitionSpec = {
            fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
        },
        label = "AffirmationFade"
    ) { targetRes ->
        Text(
            text = stringResource(id = targetRes),
            color = MaterialTheme.colorScheme.primary,
            fontFamily = Marvel,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun BluetoothStatusIndicator(status: String, iconRes: Int) {
    val isConnected = status == stringResource(id = R.string.bluetooth_connected)
    val color = if (isConnected) MintGreen else MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .padding(end = 16.dp)
            .clip(CircleShape)
            .background(color = color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(6.6.dp))
        Text(
            text = status,
            fontFamily = Marvel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ActiveSessionCard(
    timerText: String,
    selectedExercise: String?,
    onStop: () -> Unit,
    onReset: () -> Unit,
    liveReps: Int,
    isConnected: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedExercise != null && isConnected) DeepNavy else DeepNavy.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedExercise != null) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color = if (isConnected) MintGreen.copy(alpha = dotAlpha) else Color.Gray)
                        )
                    }
                    Column {
                        Text(
                            text = selectedExercise ?: "No Active Session",
                            color = if (selectedExercise != null && isConnected) MintGreen else White.copy(alpha = 0.7f),
                            fontFamily = Marvel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (selectedExercise != null && !isConnected) {
                            Text(
                                text = "Waiting for sensor...",
                                color = White.copy(alpha = 0.5f),
                                fontFamily = Marvel,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (selectedExercise != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "REPS",
                            color = White.copy(alpha = 0.5f),
                            fontFamily = Marvel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = liveReps.toString(),
                            color = MintGreen,
                            fontFamily = Marvel,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = timerText,
                color = White,
                fontFamily = Marvel,
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Stop", fontFamily = Marvel, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                    border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Reset", fontFamily = Marvel)
                }
            }
        }
    }
}

@Composable
fun ExerciseSelectorSection(
    selectedExercise: String?,
    onExerciseSelect: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSaveDescription: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Select Exercise",
                fontFamily = Marvel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
            val exercises = listOf(
                R.string.push_up, R.string.sit_up, R.string.plank,
                R.string.skipping, R.string.walking, R.string.mt_climbers,
                R.string.backs, R.string.weights, R.string.flap_jack,
                R.string.leg_raises, R.string.leg_hip_raises
            )

            exercises.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { exerciseRes ->
                        val name = stringResource(id = exerciseRes)
                        ExerciseChip(
                            name = name,
                            isSelected = selectedExercise == name,
                            onClick = { onExerciseSelect(name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Session Notes", fontFamily = Marvel) },
                placeholder = { Text(text = "How are you feeling?", fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = onSaveDescription) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    }
}

@Composable
fun ExerciseChip(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MintGreen else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Black else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name,
                fontFamily = Marvel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsSection(
    barData: BarData?,
    onPlotGraph: (String, String, String, Boolean) -> Unit,
    onClearGraph: () -> Unit,
    exerciseTypes: Array<String>,
    initialExerciseType: String,
    initialDate: String,
    isRepsTrend: Boolean,
    onTrendTypeChange: (Boolean) -> Unit
) {
    var exerciseType by remember { mutableStateOf(initialExerciseType) }
    var dateText by remember { mutableStateOf(initialDate) }
    var exerciseExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Trends",
                fontFamily = Marvel,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val isPlank = exerciseType == stringResource(id = R.string.plank)
            val isCardio = exerciseType == stringResource(id = R.string.skipping) || 
                          exerciseType == stringResource(id = R.string.mt_climbers)
            val isSitUp = exerciseType == stringResource(id = R.string.sit_up) || 
                          exerciseType == stringResource(id = R.string.leg_raises) ||
                          exerciseType == stringResource(id = R.string.leg_hip_raises) ||
                          exerciseType == stringResource(id = R.string.backs)
            
            AnimatedPillToggle(
                isSelected = isRepsTrend,
                onToggle = onTrendTypeChange,
                leftLabel = when {
                    isPlank -> "Time"
                    isCardio -> "Cadence"
                    else -> "Reps"
                },
                rightLabel = when {
                    isPlank -> "Stability"
                    isCardio -> "Reps"
                    isSitUp -> "ROM"
                    else -> "Power"
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = exerciseExpanded,
                    onExpandedChange = { exerciseExpanded = it },
                    modifier = Modifier.weight(1.0f)
                ) {
                    OutlinedTextField(
                        value = exerciseType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                        textStyle = TextStyle(fontSize = 12.sp, fontFamily = Marvel),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = exerciseExpanded,
                        onDismissRequest = { exerciseExpanded = false }
                    ) {
                        exerciseTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option, fontFamily = Marvel) },
                                onClick = {
                                    exerciseType = option
                                    exerciseExpanded = false
                                    onPlotGraph(option, "Trend", dateText, isRepsTrend)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isInspectionMode = LocalInspectionMode.current
            if (isInspectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Trend Placeholder", fontFamily = Marvel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(8.dp)
                ) {
                    if (barData != null) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            val yLabel = when {
                                isPlank -> if (isRepsTrend) "Seconds" else "Stability %"
                                isCardio -> if (isRepsTrend) "Reps/s" else "Reps"
                                isSitUp -> if (isRepsTrend) "Reps" else "Degrees (°)"
                                else -> if (isRepsTrend) "Reps" else "G-Force"
                            }
                            
                            Text(
                                text = yLabel,
                                fontFamily = Marvel,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            
                            Box(modifier = Modifier.weight(1f)) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { context ->
                                        BarChart(context).apply {
                                            description.isEnabled = false
                                            setDrawGridBackground(false)
                                            setDrawBarShadow(false)
                                            setDrawValueAboveBar(true)
                                            setPinchZoom(false)
                                            setTouchEnabled(false)
                                            
                                            xAxis.apply {
                                                setDrawGridLines(false)
                                                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                                textColor = android.graphics.Color.GRAY
                                                axisLineColor = android.graphics.Color.LTGRAY
                                                granularity = 1f
                                                setDrawLabels(false)
                                            }
                                            
                                            axisLeft.apply {
                                                setDrawGridLines(true)
                                                gridColor = android.graphics.Color.parseColor("#EEEEEE")
                                                textColor = android.graphics.Color.GRAY
                                                axisMinimum = 0f
                                            }
                                            
                                            axisRight.isEnabled = false
                                            legend.isEnabled = false
                                            animateY(1000)
                                        }
                                    },
                                    update = { view ->
                                        (view as BarChart).apply {
                                            data = barData
                                            invalidate()
                                        }
                                    }
                                )
                            }
                            
                            Text(
                                text = "Past 10 Sessions",
                                fontFamily = Marvel,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No history for this exercise", fontFamily = Marvel, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onPlotGraph(exerciseType, "Trend", dateText, isRepsTrend) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Refresh Trend", fontFamily = Marvel)
                }
                
                Surface(
                    onClick = onClearGraph,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySection(
    recentActivity: List<WorkoutSummary>,
    exerciseStats: List<ExerciseStats>,
    drillDownSummaries: List<WorkoutSummary> = emptyList(),
    drillDownExercise: String? = null,
    onStatsCardClick: (String) -> Unit = {},
    onCloseDrillDown: () -> Unit = {},
    onDeleteActivity: (WorkoutSummary) -> Unit = {},
    onTimelineItemClick: (WorkoutSummary) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 2 }) // Reduced to 2 tabs
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Timeline", fontFamily = Marvel) }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Lifetime Stats", fontFamily = Marvel) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(450.dp), 
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> GroupedTimelineList(recentActivity, onDeleteActivity, onTimelineItemClick)
                1 -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    ExerciseFrequencyGrid(exerciseStats, onStatsCardClick)
                    
                    if (drillDownExercise != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$drillDownExercise History",
                                fontFamily = Marvel,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = onCloseDrillDown) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        if (drillDownSummaries.isNotEmpty()) {
                            val historyPagerState = rememberPagerState(pageCount = { drillDownSummaries.size })
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                HorizontalPager(
                                    state = historyPagerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 0.dp),
                                    pageSpacing = 16.dp
                                ) { pageIndex ->
                                    DrillDownHistoryItem(drillDownSummaries[pageIndex])
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Simple Pager Indicator
                                Row(
                                    Modifier
                                        .height(10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(drillDownSummaries.size) { iteration ->
                                        val color = if (historyPagerState.currentPage == iteration) MintGreen else Color.LightGray.copy(alpha = 0.5f)
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(6.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No history sessions found.",
                                fontFamily = Marvel,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrillDownHistoryItem(summary: WorkoutSummary) {
    val isPlank = summary.exerciseType == stringResource(id = R.string.plank)
    val isSitUp = summary.exerciseType == stringResource(id = R.string.sit_up) || 
                  summary.exerciseType == stringResource(id = R.string.leg_raises) ||
                  summary.exerciseType == stringResource(id = R.string.leg_hip_raises) ||
                  summary.exerciseType == stringResource(id = R.string.backs)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(summary.timestamp),
                    fontFamily = Marvel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = summary.timestamp.split("T").getOrElse(1) { "" }.take(5),
                    fontFamily = Marvel,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats Grid within the card
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailMiniStat(
                    label = if (isPlank) "Hold" else "Reps",
                    value = if (isPlank) "${summary.duration}s" else "${summary.repCount}",
                    modifier = Modifier.weight(1f)
                )
                DetailMiniStat(
                    label = if (isPlank) "Stab" else "Power",
                    value = if (isPlank) String.format(Locale.getDefault(), "%.0f%%", summary.stabilityScore)
                            else String.format(Locale.getDefault(), "%.1fG", summary.maxPower),
                    modifier = Modifier.weight(1f)
                )
                DetailMiniStat(
                    label = if (isSitUp) "ROM" else "Tempo",
                    value = if (isSitUp) String.format(Locale.getDefault(), "%.2f°", summary.rangeOfMotion)
                            else String.format(Locale.getDefault(), "%.1fs", summary.cadence),
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (!isPlank) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailMiniStat(
                        label = "Symmetry",
                        value = String.format(Locale.getDefault(), "%.0f%%", summary.symmetryScore),
                        modifier = Modifier.weight(1f)
                    )
                    DetailMiniStat(
                        label = "Avg Power",
                        value = String.format(Locale.getDefault(), "%.1fG", summary.avgPower),
                        modifier = Modifier.weight(1f)
                    )
                    DetailMiniStat(
                        label = "Consistency",
                        value = String.format(Locale.getDefault(), "%.0f%%", (1f - summary.consistency.coerceAtMost(1f)) * 100f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontFamily = Marvel, fontSize = 9.sp, color = Color.Gray)
        Text(text = value, fontFamily = Marvel, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupedTimelineList(
    activity: List<WorkoutSummary>, 
    onDelete: (WorkoutSummary) -> Unit,
    onTimelineItemClick: (WorkoutSummary) -> Unit = {}
) {
    if (activity.isEmpty()) {
        EmptyHistoryState()
    } else {
        val grouped = remember(activity) { activity.groupBy { it.timestamp.take(10) } }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            grouped.forEach { (date, sessions) ->
                stickyHeader {
                    Text(
                        text = formatDate(date),
                        fontFamily = Marvel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 8.dp)
                    )
                }
                
                items(
                    items = sessions,
                    key = { it.id }
                ) { summary ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.StartToEnd) {
                                onDelete(summary)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    Box(modifier = Modifier.animateItem()) {
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = false,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> CrimsonRed
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            content = {
                                RecentActivityItem(summary, onItemClick = { onTimelineItemClick(summary) })
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.take(10))
        val now = LocalDate.now()
        when {
            date == now -> "Today"
            date == now.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
        }
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
fun ExerciseFrequencyGrid(stats: List<ExerciseStats>, onCardClick: (String) -> Unit) {
    if (stats.isEmpty()) {
        EmptyHistoryState("No workout data yet.")
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(stats) { stat ->
                FrequencyCard(stat, onClick = { onCardClick(stat.exerciseType) })
            }
        }
    }
}

@Composable
fun FrequencyCard(stat: ExerciseStats, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(MintGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stat.exerciseType,
                    fontFamily = Marvel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                Text(
                    text = "${stat.totalSessions}",
                    fontFamily = Marvel,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Total Sessions",
                    fontFamily = Marvel,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${stat.weeklySessions}",
                        fontFamily = Marvel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "This Week",
                        fontFamily = Marvel,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = formatDate(stat.lastDate).uppercase(),
                    fontFamily = Marvel,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState(message: String = "No sessions yet. Start training!") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Text(
            message,
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            fontFamily = Marvel,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecentActivityItem(summary: WorkoutSummary, onItemClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MintGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.exerciseType,
                    fontFamily = Marvel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = summary.timestamp.take(10), // Date part
                    fontFamily = Marvel,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val isPlank = summary.exerciseType == stringResource(id = R.string.plank)
                val isSitUp = summary.exerciseType == stringResource(id = R.string.sit_up) || 
                              summary.exerciseType == stringResource(id = R.string.leg_raises) ||
                              summary.exerciseType == stringResource(id = R.string.leg_hip_raises) ||
                              summary.exerciseType == stringResource(id = R.string.backs)
                val isCardio = summary.exerciseType == stringResource(id = R.string.skipping) || 
                              summary.exerciseType == stringResource(id = R.string.mt_climbers)

                val primaryMetric = when {
                    isPlank -> "${summary.duration}s"
                    isCardio -> String.format(Locale.getDefault(), "%.1f/s", if (summary.cadence > 0) 1f/summary.cadence else 0f)
                    else -> "${summary.repCount} Reps"
                }

                val secondaryMetric = when {
                    isPlank -> String.format(Locale.getDefault(), "%.0f%% Stab", summary.stabilityScore)
                    isSitUp -> String.format(Locale.getDefault(), "%.2f° ROM", summary.rangeOfMotion)
                    isCardio -> "${summary.repCount} Reps"
                    else -> String.format(Locale.getDefault(), "%.1fG Max", summary.maxPower)
                }

                Text(
                    text = primaryMetric,
                    fontFamily = Marvel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = secondaryMetric,
                    fontFamily = Marvel,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun WorkoutSummarySection(
    summary: WorkoutSummary,
    past: List<WorkoutSummary>,
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${summary.exerciseType} Summary",
                        fontFamily = Marvel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatDate(summary.timestamp.take(10)) + " • " + summary.timestamp.split("T").getOrElse(1) { "" }.take(5),
                        fontFamily = Marvel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Summary",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (past.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "First recorded session for ${summary.exerciseType}! Complete another session of ${summary.exerciseType} to compare your progress and see performance deltas.",
                            fontFamily = Marvel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isPlank = summary.exerciseType == stringResource(id = R.string.plank)
            val isSitUp = summary.exerciseType == stringResource(id = R.string.sit_up) || 
                          summary.exerciseType == stringResource(id = R.string.leg_raises) ||
                          summary.exerciseType == stringResource(id = R.string.leg_hip_raises) ||
                          summary.exerciseType == stringResource(id = R.string.backs)
            val isCardio = summary.exerciseType == stringResource(id = R.string.skipping) || 
                          summary.exerciseType == stringResource(id = R.string.mt_climbers)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isPlank) {
                    StatCard(
                        label = "Hold Time",
                        value = "${summary.duration}s",
                        delta = DynamicAnalyzerUtils.getImprovementMessage(summary.duration.toFloat(), past, "duration"),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    StatCard(
                        label = "Total Reps",
                        value = summary.repCount.toString(),
                        delta = DynamicAnalyzerUtils.getImprovementMessage(summary.repCount.toFloat(), past, "reps"),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                StatCard(
                    label = if (isPlank) "Stability" else "Peak Power",
                    value = if (isPlank) String.format(Locale.getDefault(), "%.0f%%", summary.stabilityScore) 
                            else String.format(Locale.getDefault(), "%.1fG", summary.maxPower),
                    delta = DynamicAnalyzerUtils.getImprovementMessage(
                        if (isPlank) summary.stabilityScore else summary.maxPower, 
                        past, 
                        if (isPlank) "stability" else "power"
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isPlank) {
                    StatCard(
                        label = "Stillness",
                        value = String.format(Locale.getDefault(), "%.0f%%", (1f - summary.consistency.coerceAtMost(1f)) * 100f),
                        delta = "",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.weight(1f))
                } else if (isSitUp) {
                    StatCard(
                        label = "Range of Motion",
                        value = String.format(Locale.getDefault(), "%.2f°", summary.rangeOfMotion),
                        delta = DynamicAnalyzerUtils.getImprovementMessage(summary.rangeOfMotion, past, "rom"),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Symmetry",
                        value = String.format(Locale.getDefault(), "%.0f%%", summary.symmetryScore),
                        delta = DynamicAnalyzerUtils.getImprovementMessage(summary.symmetryScore, past, "symmetry"),
                        modifier = Modifier.weight(1f)
                    )
                } else if (isCardio) {
                    StatCard(
                        label = "Cadence",
                        value = String.format(Locale.getDefault(), "%.1f/s", if (summary.cadence > 0) 1f/summary.cadence else 0f),
                        delta = "",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Consistency",
                        value = String.format(Locale.getDefault(), "%.0f%%", (1f - summary.consistency.coerceAtMost(1f)) * 100f),
                        delta = DynamicAnalyzerUtils.getImprovementMessage(summary.consistency, past, "consistency"),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Strength (Pushups, Weights, etc)
                    StatCard(
                        label = "Symmetry",
                        value = String.format(Locale.getDefault(), "%.0f%%", summary.symmetryScore),
                        delta = DynamicAnalyzerUtils.getImprovementMessage(summary.symmetryScore, past, "symmetry"),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Tempo",
                        value = String.format(Locale.getDefault(), "%.1fs/rep", summary.cadence),
                        delta = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedPillToggle(
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    leftLabel: String,
    rightLabel: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) 
            .clickable { onToggle(!isSelected) }
            .padding(4.dp)
    ) {
        val pillWidth = this.maxWidth / 2
        val offset by animateDpAsState(
            targetValue = if (isSelected) 0.dp else pillWidth,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
            label = "pillOffset"
        )

        // The Moving Selection Pill
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(pillWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        )
        
        // Labels
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = leftLabel,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontFamily = Marvel,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = rightLabel,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontFamily = Marvel,
                fontSize = 16.sp,
                fontWeight = if (!isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (!isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, delta: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 12.sp, fontFamily = Marvel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = Marvel)
            if (delta.isNotEmpty()) {
                Text(
                    text = delta,
                    fontSize = 10.sp,
                    fontFamily = Marvel,
                    color = if (delta.contains("↑")) MintGreen else CrimsonRed
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FitnessAppTheme {
        MainScreen(
            bluetoothStatus = "Connected",
            bluetoothIconRes = R.drawable.round_bluetooth_connected_24,
            timerText = "00 : 12 : 45",
            exerciseDescription = "Feeling strong!",
            onDescriptionChange = {},
            onSaveDescription = {},
            selectedExercise = "Push Up",
            onExerciseSelect = {},
            onStopTimer = {},
            onResetTimer = {},
            onPlotGraph = { _, _, _, _ -> },
            onClearGraph = {},
            exerciseTypes = arrayOf("Push Up", "Sit Up", "Plank"),
            initialExerciseType = "Push Up",
            initialDate = "10/09/2026",
            isAccelerometer = true,
            onPlotTypeChange = {},
            liveReps = 12,
            currentSummary = WorkoutSummary(
                "Push Up", 25, 2.5f, 1.2f, 0.15f, 2.2f, 45L, "2026-09-10", 85f, 92f
            ),
            pastSummaries = listOf(
                WorkoutSummary(
                    "Push Up", 20, 2.0f, 1.0f, 0.2f, 2.5f, 50L, "2026-09-09", 80f, 90f
                )
            ),
            isBluetoothConnected = true,
            isScanning = false,
            onReconnect = {},
            barData = null,
            recentActivity = emptyList(),
            exerciseStats = emptyList(),
            drillDownSummaries = emptyList(),
            drillDownExercise = null,
            onStatsCardClick = {},
            onCloseDrillDown = {}
        )
    }
}
