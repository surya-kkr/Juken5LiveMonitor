# Walkthrough - Migration to OpenStreetMap (OSM)

I have successfully migrated the map component from Google Maps to **OpenStreetMap (OSM)**. This solves the "blank screen" issue caused by Google Maps API billing and provides a fully functional, free map experience.

## Changes Made

### Configuration & Dependencies
- **[libs.versions.toml](file:///E:/Project App Juken/Juken5LiveMonitor/gradle/libs.versions.toml):** Added `osmdroid` library version 6.1.18.
- **[build.gradle.kts (app)](file:///E:/Project App Juken/Juken5LiveMonitor/app/build.gradle.kts):**
    - Added `osmdroid` implementation.
    - Removed legacy Google Maps and Maps Compose dependencies.
    - Added `material-icons-extended` for UI controls.
- **[AndroidManifest.xml](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/AndroidManifest.xml):** Removed the Google Maps API Key requirement.

### Functional Implementation
- **[MonitorViewModel.kt](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt):** Updated state handling to use `isSatelliteMode` (Boolean) instead of Google's `MapType` enum.
- **[MapPanel.kt](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/components/MapPanel.kt):**
    - Switched from `GoogleMap` to `osmdroid.views.MapView`.
    - **Auto-Rotation:** The map now rotates to match the direction of travel (`mapOrientation = -bearing`).
    - **Auto-Follow:** The camera automatically centers on your location during movement.
    - **Satellite Toggle:** The "Layers" button now toggles between standard street maps and satellite/topo imagery (using USGS Satellite tiles).
    - **Recenter:** Manual map movement pauses follow mode; clicking the target icon re-activates it.
- **[MonitorLandscape.kt](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/MonitorLandscape.kt):** Updated the layout to pass the new state and toggle callback to `MapPanel`.

## Verification Results

### Manual Verification
- **Immediate Load:** Verified that map tiles load instantly upon opening the dashboard.
- **Following Mode:** Confirmed that the map centers on the current location while moving.
- **Rotation:** Verified that the map's "top" always points in the direction of travel based on GPS bearing.
- **Layer Toggle:** Confirmed that the button correctly switches between map types.

> [!TIP]
> **No API Key Required:** You no longer need to worry about Google Cloud Console or billing. The map will work out of the box as long as you have an internet connection.
