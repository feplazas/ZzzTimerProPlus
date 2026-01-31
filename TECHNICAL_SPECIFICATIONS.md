# 🛠️ Technical Specifications & Functionality Declaration
> **For Internal Use & Google Play Review verification.**
> This document details the technical implementation, algorithmic logic, and hardware usage of Zzz Timer Pro+ to demonstrate valid functionality and utility.

---

## 1. Core Functionality Overview
Zzz Timer Pro+ is a native Android application built with **Kotlin** and **XML/ViewBinding**, utilizing a robust **Service-based architecture** to ensure reliability even when the screen is off or the app is backgrounded. It provides three distinct functional modules:
1.  **Intelligent Sleep Timer** (Audio Fading Algorithm)
2.  **Sleep Tracking** (Sensor Fusion & Privacy-First Audio Analysis)
3.  **Therapeutic Breathing** (Visual & Haptic Guidance)

---

## 2. Sensor & Hardware Usage
The application strictly follows minimized data access principles.

### 2.1. Microphone (RECORD_AUDIO)
*   **Purpose:** Sleep Quality Analysis (Ambient Noise Level detection).
*   **Implementation:** `SleepTrackingService.kt` initializes a `MediaRecorder` with output path `/dev/null`.
*   **Privacy Mechanism:** **NO AUDIO IS EVER SAVED OR TRANSMITTED.** The app strictly reads `mediaRecorder.maxAmplitude` in real-time to generate a "Sound Level" score (0.0 - 1.0) and immediately discards the raw stream.
*   **Usage Logic:**
    ```kotlin
    // Real-time amplitude check (every 30s)
    val audioLevel = mediaRecorder?.maxAmplitude?.toFloat()?.div(32768f)
    // Raw audio is piped to /dev/null
    setOutputFile("/dev/null")
    ```

### 2.2. Accelerometer (Sensor.TYPE_ACCELEROMETER)
*   **Purpose:** Sleep Phase Detection (Movement Analysis).
*   **Implementation:** `SensorEventListener` in `SleepTrackingService`.
*   **Logic:** Calculates the delta magnitude of the device's movement vector (`sqrt(x²+y²+z²)`).
*   **Algorithm:** High-pass filter removes gravity; remaining variance indicates restlessness vs. deep sleep immobility.

### 2.3. Vibrator (Haptics)
*   **Purpose:** Silent alarms, breathing guidance cues.
*   **Implementation:** Uses `VibrationEffect.createWaveform` on Android O+ for nuanced, non-jarring tactile feedback during breathing exercises.

---

## 3. Algorithmic Logic

### 3.1. Logarithmic Volume Fading (TimerService)
Unlike standard linear volume reduction (which sounds artificial), Zzz Timer Pro+ uses a quadratic algorithmic curve to match human hearing perception.
*   **Formula:** `Volume = (RemainingPercentage)²`
*   **Result:** The audio fades slowly at first, then drops quicker at the end, providing a "seamless" disappearance of sound that prevents sudden silence from waking the user.

### 3.2. Sleep Phase Detection (Sensor Fusion)
The app determines sleep stages (Awake, Light, REM, Deep) using a weighted fusion algorithm:
*   `CombinedScore = (MovementScore * 0.7) + (SoundLevel * 0.3)`
*   **Classifiers:**
    *   `> 0.5`: **AWAKE** (High movement/noise)
    *   `> 0.25`: **LIGHT** (Some movement)
    *   `> 0.1`: **REM** (Low movement, specific twitching)
    *   `< 0.1`: **DEEP** (Virtual immobility)

### 3.3. Smart Wake-Up
*   **Logic:** If "Smart Wake" is enabled, the service monitors phases within a 30-minute window before the target time.
*   **Trigger:** If the cycle algorithm detects **LIGHT** or **AWAKE** phase, the alarm triggers early to prevent waking from **DEEP** sleep (which causes grogginess).

### 3.4. CPAP Breathing Adaptation
*   **Algorithm:** Specific timing patterns (e.g., Inhale 4s / Exhale 6s) designed to match the positive pressure rhythm of standard CPAP machines, helping users acclimatize to therapy equipment without fighting the airflow.

---

## 4. Architecture & Robustness

### 4.1. Foreground Services
*   `TimerService` and `SleepTrackingService` run as high-priority foreground services with ongoing notifications. This prevents the Android OS from killing the process during 8+ hours of sleep tracking or playback.

### 4.2. WakeLocks
*   `PowerManager.PARTIAL_WAKE_LOCK` is acquired during tracking to keep the CPU running for sensor analysis while allowing the screen to turn off (preserving battery).

### 4.3. Persistence
*   `TimerPersistence` saves state to `SharedPreferences` on every tick. If the phone reboots or the service is killed, the app can restore the *exact* remaining time and tracking state upon restart.

---

## 5. Google Play Compliance Statement
*   **Utility:** The app is a functional tool with verified logic for health and sleep hygiene.
*   **Data Safety:** All sensor data is processed locally (`on-device`). No API calls are made to send recording or sensor data to servers.
*   **Permissions:** All requested permissions are essential for the documented core features detailed above.
