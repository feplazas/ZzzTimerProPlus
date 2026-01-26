# Release Checklist for Google Play

## 1. Build & Signing
- [ ] **Keystore**: Ensure you have `release-key.jks` and `keystore.properties` populated.
  - If missing, run:
    ```powershell
    keytool -genkeypair -v -keystore app/release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0
    ```
  - `keystore.properties` content:
    ```properties
    storeFile=release-key.jks
    storePassword=YOUR_PASSWORD
    keyAlias=key0
    keyPassword=YOUR_PASSWORD
    ```
- [ ] **Build Command**:
  ```powershell
  ./gradlew clean bundleRelease
  ```
- [ ] **Verify Artifact**: Check `app/build/outputs/bundle/release/app-release.aab`. Size should be optimized (checking shrinkResources).

## 2. Play Console Setup
### App Content & Data Safety
- **Data Collection**:
  - **Audio/Microphone**: Yes, collected.
    - *Purpose*: Sleep Tracking / Snore Detection.
    - *Shared?*: No (processed locally on device? If so, state "My app does not transfer this data off the device").
  - **Location**: Yes (if BLE/Sensors use it, otherwise remove permission). Manifest shows `play-services-location`. Check if actually needed. 
- **Permissions**:
  - `SCHEDULE_EXACT_ALARM`: Critical for Timer/Alarm accuracy. Justification: "The app is a Timer/Alarm app and requires precise triggering."
  - `FOREGROUND_SERVICE`: Essential for timer background execution.

### Privacy Policy
- URL needed: `{PRIVACY_POLICY_URL}`
- Must disclose:
  - Usage of Microphone for sleep tracking.
  - Usage of foreground services.
  - No data sales (if true).

## 3. Store Listing
- **App Name**: Zzz Timer Pro+
- **Short Description**: Premium Sleep Timer, Alarm & Sleep Tracker with immersive starry themes.
- **Full Description**: (See store_listing.md)
- **Graphics**:
  - Icon: 512x512 png (32-bit color).
  - Feature Graphic: 1024x500 png.
  - Screenshots: Phone (2 min), 7-inch Tablet, 10-inch Tablet.

## 4. Testing
- [ ] Run `veridex` tool on APK (optional) to check hidden API usage.
- [ ] Upload to **Internal Testing** track first.
- [ ] Test on Android 12, 13, 14, 15 devices (emulator or real).
