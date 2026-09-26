# Bluetooth Fitness Application - Technical Documentation

## 1. Architecture Overview
The project follows a **Clean Architecture** approach using a hybrid **MVVM (Model-View-ViewModel)** pattern. 

### Hybrid Implementation
- **View Layer**: Implemented in **Kotlin** using **Jetpack Compose** (Material 3). This provides a declarative, high-performance UI.
- **DSP & Data Layer**: Implemented in **Java** for numerical stability, low-level efficiency, and backward compatibility.
- **Dependency Injection**: Powered by **Dagger 2**. Core components like `WorkoutSession` and `ExerciseRepository` are injected into ViewModels using constructor injection.
- **Session Management**: A dedicated `WorkoutSession` class encapsulates live workout state, held by `ExerciseViewModel` to survive configuration changes.

---

## 2. Core Modules

### 2.1 Bluetooth Module (`BluetoothLeService.java`)
Manages the lifecycle of the connection to the physical fitness sensor.
- **GATT Profile**: Communicates with a custom RP2040 GATT service to stream 6-axis accelerometer and gyroscope data.
- **Widget Integration**: Updates shared preferences upon connection/disconnection to keep Home Screen widgets in sync.

### 2.2 DSP & Analysis Engine (`com.atom.bluetoothfitnessapplication.dsp`)
The signal processing "brain" of the application that converts raw 6-axis IMU streams into physical fitness metrics off the UI thread (`Schedulers.computation()`).

#### A. Zero-Phase Butterworth Filter (`ButterworthFilter.java`)
- **SciPy Equivalence**: Implements a 2nd-order zero-phase 2-pass (forward + backward) low-pass Butterworth filter matching Python `scipy.signal.filtfilt`.
- **Phase Preservation**: Completely eliminates high-frequency sensor noise and vibration without introducing group delay or phase shifts.

#### B. IMU Repetition Detector (`IMURepDetector.java`)
- **Multi-Axis Peak Engine**: Finds prominent peaks and troughs on filtered acceleration or gyroscope channels.
- **Debounce Window**: Enforces dynamic minimum sample distance constraints to prevent double-counting split crests.

#### C. Range of Motion Calculator (`RangeOfMotionCalculator.java`)
- **Trapezoidal Numerical Integration**: Integrates angular velocity ($\text{deg/s}$) over midpoint rep windows $\frac{\text{peak}_i + \text{peak}_{i+1}}{2}$:
  $$\text{ROM} = \frac{1}{2} \sum_{k=\text{start}}^{\text{end}-1} \frac{|y_k| + |y_{k+1}|}{2} \cdot \Delta t$$
- **Raw Deg/s Input**: Operates directly on degrees per second without extraneous conversion factors, providing exact physical ROM in degrees ($\text{deg}$).

#### D. Dynamic Analytics Utility (`DynamicAnalyzerUtils.java`)
- **Dynamic Sample Rate Derivation**: Calculates exact session sampling frequency $f_s = \frac{\text{totalSamples}}{\text{durationSec}}$ per workout.
- **Two-Pass Adaptive Repetition Calibration**: Measures actual user cadence ($\Delta t$) from preliminary peaks to dynamically adjust minimum sample distance:
  $$\text{minDistance} = \text{round}(\text{safetyFactor} \times f_s \times \text{minRepDurationSec})$$
- **ExerciseCategory Classifier**: Locale-resilient, unified exercise category classifier (`SIT_UP`, `PUSH_UP`, `LEG_RAISES`, `SKIPPING`, `BACKS`, `GENERIC`).

---

## 3. Supported Exercises & DSP Mapping

| Exercise | Primary Rep Peak Channel | Primary ROM Integration Channel | Filter Cutoff | Baseline Min Duration |
| :--- | :--- | :--- | :--- | :--- |
| **Sit-Ups** | Gyroscope X & Acceleration Y | Gyroscope X ($\text{deg/s}$) | $2.5\text{ Hz}$ | $1.2\text{s}$ |
| **Push-Ups** | Acceleration Z & Gyroscope X | Gyroscope X ($\text{deg/s}$) | $2.0\text{ Hz}$ | $0.8\text{s}$ |
| **Leg Raises** | Acceleration Z & Gyroscope X | Gyroscope X ($\text{deg/s}$) | $2.5\text{ Hz}$ | $1.0\text{s}$ |
| **Leg Hip Raises** | Acceleration Z & Gyroscope X | Gyroscope X ($\text{deg/s}$) | $2.5\text{ Hz}$ | $1.0\text{s}$ |
| **Backs** | Acceleration Z & Gyroscope X | Gyroscope X ($\text{deg/s}$) | $2.5\text{ Hz}$ | $1.0\text{s}$ |
| **Skipping** | Acceleration Magnitude | Gyroscope Magnitude ($\text{deg/s}$) | $3.0\text{ Hz}$ | $0.35\text{s}$ |
| **Planks** | Gyroscope Magnitude (Stillness) | N/A (Hold Time & Stability) | N/A | Static |

---

## 3.1 Biomechanical & Fitness Metrics Definitions

The application computes 6 primary fitness metrics stored in `WorkoutSummary.java`:

### 1. Range of Motion (ROM - `rangeOfMotion`)
- **Meaning**: Physical angular displacement (rotation in degrees, $\text{deg}$) traversed by the body or limb during each repetition (e.g., flexion in Sit-Ups or Leg Raises).
- **Calculation (`RangeOfMotionCalculator.java`)**:
  Trapezoidal integration of filtered 1D angular velocity ($\text{deg/s}$) over midpoint rep boundary windows $\frac{\text{peak}_i + \text{peak}_{i+1}}{2}$:
  $$\text{AngularDisplacement} = \sum_{k=\text{start}}^{\text{end}-1} \frac{|y_k| + |y_{k+1}|}{2} \cdot \Delta t$$
  $$\text{ROM} = \frac{\text{AngularDisplacement}}{2}$$
  The summary records the average ROM in degrees across all detected repetitions in the set.

### 2. Symmetry Score (`symmetryScore`)
- **Meaning**: Evaluates directional alignment and balance during movement. A score of $100\%$ indicates perfectly balanced execution, while lower scores signal rotational twisting or side-to-side body drift.
- **Calculation (`calculateSymmetryScore`)**:
  Ratio of net 3D directional rotational drift to total absolute motion across all 3 gyroscope axes ($X, Y, Z$):
  $$\text{TotalMotion} = \sum (|g_x| + |g_y| + |g_z|)$$
  $$\text{NetDrift} = \left| \sum g_x \right| + \left| \sum g_y \right| + \left| \sum g_z \right|$$
  $$\text{BiasRatio} = \frac{\text{NetDrift}}{\text{TotalMotion}}$$
  $$\text{SymmetryScore} = \max\left(0, \; (1.0 - 2 \cdot \text{BiasRatio}) \times 100\%\right)$$

### 3. Stability Score (`stabilityScore`)
- **Meaning**: Quantifies posture stillness and core control during static holds (such as Planks) or dynamic repetitions. Higher scores ($0 - 100\%$) indicate steady, shake-free engagement.
- **Calculation (`calculateStabilityScore`)**:
  Exponential decay model based on the variance ($\sigma^2$) of the rotational gyroscope magnitude stream:
  $$\text{Variance } (\sigma^2) = \frac{1}{N} \sum_{i=1}^{N} (|\mathbf{g}_i| - \mu_{|\mathbf{g}|})^2$$
  $$\text{StabilityScore} = e^{-k \cdot \sigma^2} \times 100\% \quad \text{where } k = 8.0$$

### 4. Cadence (`cadence`)
- **Meaning**: Repetition pacing representing average seconds elapsed per repetition, or tempo during cardio exercises like Skipping and Mountain Climbers.
- **Calculation (`processWorkoutSummaryReactive`)**:
  $$\text{Cadence} = \frac{\text{Duration (seconds)}}{\text{Repetition Count}}$$

### 5. Consistency (`consistency`)
- **Meaning**: Measures how uniform the user's explosive thrust and force output are throughout the set. A lower consistency value (standard deviation) indicates smooth, repeatable movement across reps.
- **Calculation (`calculateStdDev`)**:
  Population standard deviation ($\sigma$) of the 3D acceleration magnitude series ($G$):
  $$\text{Consistency } (\sigma) = \sqrt{\frac{1}{N} \sum_{i=1}^{N} (|\mathbf{a}_i| - \mu_{|\mathbf{a}|})^2}$$

### 6. Average Power (`avgPower`) & Max Power (`maxPower`)
- **Meaning**: Measures peak and average instantaneous acceleration forces produced during exercise in Earth gravitational units ($G$).
- **Calculation (`calculateMean` & `calculateMax`)**:
  - **Max Power (`maxPower`)**: Peak instantaneous acceleration magnitude reached during the session:
    $$\text{MaxPower} = \max_i \sqrt{a_{x,i}^2 + a_{y,i}^2 + a_{z,i}^2}$$
  - **Average Power (`avgPower`)**: Arithmetic mean acceleration magnitude across all samples:
    $$\text{AvgPower} = \frac{1}{N} \sum_{i=1}^{N} \sqrt{a_{x,i}^2 + a_{y,i}^2 + a_{z,i}^2}$$

---

## 4. Storage Layer (`FitnessExerciseDatabase.java`)
- **Version**: 8 (Room Persistence Library)
- **Entities**: `AccelerometerData`, `ExerciseDescription`, `Weights`, `Skipping`, `PushUp`, `Backs`, `MountainClimbers`, `Flapjacks`, `SitUps`, `Walking`, `Plank`, `LegRaises`, `LegHipRaises`, `WorkoutSummary`.
- **Migrations**: Incremental SQLite migrations (`MIGRATION_1_2` through `MIGRATION_7_8`) with destructive fallback safety.

---

## 5. UI & Android 12+ Splash Screen

### 5.1 AndroidX Splash Screen (`androidx.core:core-splashscreen`)
- **Theme**: `Theme.App.Starting` inheriting from `Theme.SplashScreen`.
- **Icon Assets**: Enclosed in `@drawable/ic_splash_logo` at exact $84\text{dp}-108\text{dp}$ safe bounds to fit Android 12's $160\text{dp}$ circular mask without clipping.
- **Theme Swap**: `installSplashScreen()` in `MainActivity.kt` switches automatically to `postSplashScreenTheme` (`Theme.BluetoothFitnessApplication`).

### 5.2 Dashboard Layout (`MainScreen.kt`)
- **History Pager**: `HorizontalPager` with **Timeline** (grouped activity) and **Lifetime Stats** (7-day weekly frequency).
- **ActiveSessionCard**: Real-time timer and rep counter.
- **InsightsSection**: Interactive MPAndroidChart bar chart with adaptive axis units (Reps, Degrees, Hold Time, Cadence).

---

## 6. Asynchronous & Reactive Execution
- **Off-Thread Processing**: All Butterworth filtering, peak detection, and ROM trapezoidal integration execute on `Schedulers.computation()`.
- **Chained Finalization**: Session stop triggers `processWorkoutSummaryReactive` which executes Room batch inserts and UI updates on `AndroidSchedulers.mainThread()`.
