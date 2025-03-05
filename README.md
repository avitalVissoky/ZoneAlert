# ZoneAlert

## Overview
This Android application runs a foreground service that continuously monitors and records location data. The data is stored in **SharedPreferences**, and upon a refresh request from the **Heatmap App**, the recorded data is sent to **Firebase**.

## Features
- Runs a **Foreground Service** to track location persistently.
- Stores location data in **SharedPreferences** for efficient local access.
- Listens for a refresh request from the **Heatmap App** and sends data to **Firebase**.
- Uses a **Boot Receiver** to restart the service after device reboots.

## How It Works
1. **Foreground Service Activation:**
   - The service starts running when the user enables location tracking.
   - Runs persistently in the background using `startForegroundService()`.
   
2. **Location Storage:**
   - The service saves real-time location updates in **SharedPreferences**.

3. **Data Request & Firebase Sync:**
   - The heatmap application triggers a **data request** by updating a specific flag in **Firebase Database**.
   - This application listens for changes in the database.
   - Upon detecting a request, it uploads the stored relevant location data to Firebase.

## Technologies Used
- **Android Foreground Service** for continuous location tracking.
- **SharedPreferences** for lightweight data storage.
- **Firebase Realtime Database** for syncing location data.
- **Broadcast Receiver** to restart service on boot.

## Permissions Required
The app requires the following permissions for full functionality:

```xml
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## Stopping the Service
- The service can be manually stopped via a button in the UI.
- The service stops if the user revokes location permissions.

## Auto Restart on Boot
- The **Boot Receiver** listens for device reboots.
- It automatically restarts the location tracking service.

## ScreenShots
<img src="https://github.com/user-attachments/assets/2f195eb2-df5d-4cfe-b7bf-a8cbf3226bec" style="height:400px;"/>
<img src="https://github.com/user-attachments/assets/8a63b2f5-01cd-4b92-a4a8-689ba872037b" style="height:400px;"/>
<img src="https://github.com/user-attachments/assets/2f195eb2-df5d-4cfe-b7bf-a8cbf3226bec" style="height:400px;"/>
