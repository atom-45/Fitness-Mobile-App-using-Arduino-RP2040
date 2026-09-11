# Bluetooth Fitness Application - Technical Documentation

## 1. Architecture Overview
The project follows a **Clean Architecture** approach using a hybrid **MVVM (Model-View-ViewModel)** pattern. 

### Hybrid Implementation
- **View Layer**: Implemented in **Kotlin** using **Jetpack Compose**. This allows for high-performance, declarative UI and smooth animations.
- **Business Logic & Data Layer**: Implemented in **Java** for stability and legacy support.
- **Dependency Injection**: Powered by **Dagger 2**. Key components like `WorkoutSession` and `ExerciseRepository` are injected into ViewModels using constructor injection.
- **Session Management**: A dedicated `WorkoutSession` class encapsulates live workout state, held by `ExerciseViewModel` to survive configuration changes.

---

## 2. Core Modules

### 2.1 Bluetooth Module (`BluetoothLeService.java`)
Manages the lifecycle of the connection to the physical fitness sensor.
- **GATT Profile**: Communicates with a custom RP2040 service.
- **Widget Integration**: Updates shared preferences upon connection/disconnection to keep Home Screen widgets in sync.

### 2.2 Analysis Engine (`AnalyzerUtils.java`)
The "brain" of the application that converts raw sensor data into fitness metrics.
- **Rep Counting**: Uses a peak-detection algorithm with a 1.5G threshold.
- **Symmetry Score**: An **orientation-agnostic** algorithm that calculates rotational drift magnitude across all three axes (X, Y, Z).
- **Adaptive Logic**: Dynamically switches metrics based on exercise type (e.g., Hold Time for Planks, Cadence for Cardio).

### 2.3 Storage Layer (`FitnessExerciseDatabase.java`)
- **Version**: 4
- **Recent Weekly Frequency**: SQL logic (`SUM(CASE WHEN timestamp >= date('now', '-7 days') THEN 1 ELSE 0 END)`) provides real-time counts of exercise sessions within the last 7 days.

---

## 3. UI Implementation

### 3.1 Dashboard Layout
The UI is organized into modular sections within `MainScreen.kt`:
- **History Pager (Top)**: A `HorizontalPager` containing **Timeline** (grouped activity) and **Lifetime Stats** (frequency cards with weekly counts).
- **ActiveSessionCard**: Displays live timer and rep counts with pulsing animation.
- **InsightsSection**: Houses the **Performance Trend** bar chart with adaptive axis labels.

### 3.2 Home Screen Widgets (Jetpack Glance)
Implemented using the **Glance** framework for Compose-like widget development:
- **QuickStartWidget**: Provides one-tap shortcuts to popular exercises and shows sensor status.
- **PerformanceSnapshotWidget**: Displays a summary of the most recent session with a motivational progress ring.
- **Resilience**: State is synchronized via a `WidgetHelper` and `SharedPreferences` to ensure widgets reflect app state instantly.

---

## 4. Permissions & Lifecycle
- **Android 12-16 Support**: Handles `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, and `POST_NOTIFICATIONS`.
- **Configuration Resilience**: Use of ViewModels and the standalone `WorkoutSession` ensures the timer never resets during screen rotations.

---

## 5. Reactive Logic (RxJava)
- **Chained Finalization**: When a session stops, the app uses `flatMapCompletable` and `andThen` to:
    1. Fetch previous summaries.
    2. Save the current session results.
    3. Refresh the recent activity list and lifetime stats.
- **UI State**: The `MainUiState` class bridges Java RxJava streams to Compose `mutableStateOf` properties.
