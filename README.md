# Bluetooth Fitness Application

A modern Android application designed to track and analyze physical exercises using real-time data from a Bluetooth Low Energy (BLE) sensor (specifically optimized for RP2040-based hardware).

## 🚀 Key Features

*   **Real-time Tracking**: Connects to BLE fitness sensors to capture 6-axis accelerometer and gyroscope data.
*   **Intelligent Analysis**:
    *   **Live Rep Counting**: High-precision peak-detection for push-ups, sit-ups, and more.
    *   **Exercise-Specific Metrics**: Automatically adapts its tracking logic for different movements:
        *   **Static Stability (Plank)**: Tracks hold time and stillness.
        *   **Dynamic Strength**: Measures power (G-force), symmetry (balance), and tempo.
        *   **Cardio (Skipping)**: Analyzes cadence (reps/sec) and consistency.
*   **Training Momentum (New)**: A high-level overview of your progress located at the top of the dashboard:
    *   **Timeline View**: Activity grouped by day (Today, Yesterday, etc.) to track streaks.
    *   **Lifetime Stats**: Exercise frequency cards showing total sessions and **Weekly Frequency**.
*   **Home Screen Widgets**:
    *   **Quick Start**: Check sensor readiness and launch common exercises with one tap.
    *   **Performance Snapshot**: View your last session's results and progress ring directly on your home screen.
*   **Performance Trends**: Visualizes progress over the last 10 sessions using an interactive bar chart.
*   **Modern UI**: Built entirely with **Jetpack Compose** (Material 3), featuring a theme-aware "Mint & Navy" dark/light palette.

## 🛠 Technical Stack

*   **UI Framework**: Jetpack Compose (Kotlin)
*   **App Widgets**: Jetpack Glance (Compose for Widgets)
*   **Architecture**: Hybrid MVVM with Reactive UI & Dagger Dependency Injection.
*   **Local Database**: Room Persistence Library (Version 4)
*   **Asynchronous Logic**: RxJava 3 for database chaining and sensor processing.
*   **Networking/BLE**: Bluetooth Low Energy (GATT) with support for Android 12-16 permissions.
*   **Data Visualization**: MPAndroidChart.

## 📱 Screenshots

*(Coming Soon - You can view the layout in the `MainScreenPreview` within Android Studio)*

## 🛠 Getting Started

### Prerequisites
*   Android Studio Quail or newer.
*   Android Device with Bluetooth 5.0 support (API Level 31+ recommended).
*   Compatible BLE Fitness Sensor (RP2040 based).

### Installation
1.  Clone the repository.
2.  Open in Android Studio.
3.  Build and deploy the `:app` module to your device.

## ⚖️ License
This project is licensed under the MIT License - see the LICENSE file for details.
