# Bluetooth Fitness Application - Technical Documentation

## 1. Architecture Overview
The project follows a **Clean Architecture** approach using a hybrid **MVVM (Model-View-ViewModel)** pattern. 

### Hybrid Implementation
- **View Layer**: Implemented in **Kotlin** using **Jetpack Compose**. This allows for high-performance, declarative UI and smooth animations.
- **Business Logic & Data Layer**: Implemented in **Java**. This includes ViewModels, Repositories, DAOs, and the BLE Service.
- **State Management**: A specialized `MainUiState` class acts as the bridge, using Compose `mutableStateOf` properties to trigger recomposition when Java-based logic updates the data.

---

## 2. Core Modules

### 2.1 Bluetooth Module (`BluetoothLeService.java`)
Manages the lifecycle of the connection to the physical fitness sensor.
- **GATT Profile**: Communicates with a custom RP2040 service.
- **Broadcast System**: Uses package-targeted broadcasts to ensure data reliability on Android 14+.
- **Data Format**: Receives a 6-element float array containing [AccX, AccY, AccZ, GyroX, GyroY, GyroZ].

### 2.2 Analysis Engine (`AnalyzerUtils.java`)
The "brain" of the application that converts raw sensor data into fitness metrics.
- **Rep Counting**: Uses a peak-detection algorithm with a 1.5G threshold and minimum sample distance to filter out noise.
- **Stability Score**: Calculated by measuring the variance of the gyroscope magnitude. Lower variance equals a higher stability percentage.
- **Symmetry Score**: An orientation-agnostic algorithm that compares the "Net Rotational Drift" against "Total Absolute Motion" across all three axes.
- **Adaptive Logic**: Dynamically switches metrics based on exercise type (e.g., Duration for Planks vs. Reps for Push-ups).

### 2.3 Storage Layer (`FitnessExerciseDatabase.java`)
- **Version**: 4
- **Schema**:
    - `PushUp`, `SitUps`, etc.: Raw sensor data tables for plotting (legacy support).
    - `workout_summaries`: Stores the finalized results of every session (Rep count, Max Power, Stability, Symmetry, Duration).
- **Migration**: Includes automated migrations to support the newer performance tracking fields.

---

## 3. UI Implementation

### 3.1 Jetpack Compose UI
The UI is organized into modular sections within `MainScreen.kt`:
- **ActiveSessionCard**: Displays live timer and rep counts with a pulsing connectivity indicator.
- **InsightsSection**: Houses the **Performance Trend** bar chart.
- **RecentActivitySection**: A scrollable list of past sessions.
- **ExerciseSelectorSection**: Collapsible grid for choosing the current workout.

### 3.2 Data Visualization
- Uses **MPAndroidChart** integrated via `AndroidView` interop.
- **Trend Charts**: Bar charts that show progress over the last 10 sessions.
- **Styling**: Cubic-bezier smoothing and specialized fill colors matching the "Mint & Navy" palette.

---

## 4. Permissions & Lifecycle
The app handles complex Android Bluetooth permissions across different OS versions:
- **Android 12 (API 31)**: Requires `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.
- **Android 13 (API 33)**: Requires `POST_NOTIFICATIONS` for connection alerts.
- **Android 14-16**: Uses `RECEIVER_NOT_EXPORTED` flag for broadcast safety.

---

## 5. Reactive Logic (RxJava)
Asynchronous operations are handled via **RxJava 3**:
- **Chained Finalization**: When a session stops, the app fetches past data, calculates deltas, saves the new summary, and refreshes the history in a single non-blocking stream.
- **Threading**: Database operations run on `Schedulers.io()`, calculations on `Schedulers.computation()`, and UI updates on `AndroidSchedulers.mainThread()`.
