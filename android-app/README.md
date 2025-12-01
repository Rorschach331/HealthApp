# Health App (Android)

This is a Native Android implementation of the Health App using Kotlin and Jetpack Compose.

## Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK API 34

## Setup

1. Open this folder (`android-app`) in Android Studio.
2. Allow Android Studio to sync Gradle and generate necessary wrapper files.
3. Ensure your backend (`server-dotnet`) is running.
   - By default, the app connects to `http://10.0.2.2:3000/` which is the localhost alias for Android Emulator.
   - If running on a physical device, update `app/src/main/java/com/example/healthapp/api/RetrofitClient.kt` with your computer's local IP address (e.g., `http://192.168.1.x:3000/`).

## Features

- **Record**: Add new blood pressure records.
- **History**: View list of records with status indicators.
- **Trends**: View blood pressure trends in a chart.

## Architecture

- **MVVM**: Model-View-ViewModel pattern.
- **Jetpack Compose**: Modern UI toolkit.
- **Retrofit**: Type-safe HTTP client.
