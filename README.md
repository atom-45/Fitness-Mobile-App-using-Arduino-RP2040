# Bluetooth Fitness Application

A modern Android application designed to track and analyze physical exercises using real-time data from a Bluetooth Low Energy (BLE) sensor (specifically optimized for RP2040-based hardware).

## 🚀 Key Features

*   **Real-Time Sensor Tracking**: Connects via BLE GATT to capture 6-axis accelerometer and gyroscope motion streams.
*   **Advanced Digital Signal Processing (DSP)**:
    *   **SciPy-Grade Butterworth Filtering**: 2nd-order zero-phase 2-pass (`filtfilt`) low-pass filtering to eliminate motion jitter without phase shift.
    *   **Dynamic Sample Rate Derivation**: Calculates exact session sampling frequency ($f_s$) dynamically per workout.
    *   **Two-Pass Adaptive Repetition Calibration**: Measures live user cadence to dynamically calibrate peak debounce windows.
    *   **Physical Range of Motion (ROM)**: Computes angular displacement in degrees ($\text{deg}$) via trapezoidal numerical integration over midpoint windows.
*   **Comprehensive Exercise Support (11 Exercises)**:
    *   **Flexion / Extension (Sit-Ups, Leg Raises, Leg Hip Raises, Backs)**: Evaluates angular velocity and vertical acceleration.
    *   **Dynamic Strength (Push-Ups)**: Measures thrust power (G-force), vertical displacement, and peak acceleration.
    *   **Cardio & Plyometrics (Skipping)**: Evaluates acceleration magnitude peaks and rotational gyroscope magnitude.
    *   **Static Core (Planks)**: Tracks hold time, stability score, and stillness.
*   **Android 12+ Splash Screen**: Smooth theme transition using `androidx.core:core-splashscreen` with centered app logo vector assets.
*   **Training Momentum & Dashboard**:
    *   **Timeline View**: Activity grouped by day (Today, Yesterday, etc.) to track streaks.
    *   **Lifetime Stats**: Exercise frequency cards showing total sessions and 7-day weekly frequency.
*   **Home Screen Widgets (Jetpack Glance)**:
    *   **Quick Start**: Check sensor readiness and launch exercises with one tap.
    *   **Performance Snapshot**: View your last session's results and progress ring directly on your home screen.
*   **Performance Trends**: Visualizes progress over the last 10 sessions using an interactive chart.

## 🛠 Technical Stack

*   **UI Framework**: Jetpack Compose (Kotlin, Material 3)
*   **App Widgets**: Jetpack Glance (Compose for Widgets)
*   **Architecture**: Hybrid MVVM with Reactive UI & Dagger Dependency Injection
*   **DSP Engine**: Custom `com.atom.bluetoothfitnessapplication.dsp` package (Java, RxJava 3)
*   **Local Database**: Room Persistence Library (Version 8)
*   **Asynchronous Processing**: RxJava 3 (`Schedulers.computation()`) offloading DSP filtering off the UI thread
*   **Networking/BLE**: Bluetooth Low Energy (GATT) supporting Android 12–16 permissions (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`)
*   **Data Visualization**: MPAndroidChart

## 🛠 Getting Started

### Prerequisites
*   Android Studio Quail or newer.
*   Android Device with Bluetooth 5.0 support (API Level 31+ recommended).
*   Compatible BLE Fitness Sensor (RP2040 based).

### Installation
1. Clone the repository.
2. Open in Android Studio.
3. Build and deploy the `:app` module to your device.

## ⚖️ License
This project is licensed under the MIT License - see the LICENSE file for details.
