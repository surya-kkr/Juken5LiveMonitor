# Implementation Plan - Switch to OpenStreetMap (OSM)

Due to persistent issues with Google Maps API billing/verification, we will migrate the map component to **OpenStreetMap (OSM)** using the **osmdroid** library. This will provide a free, no-API-key-required map experience while maintaining features like auto-rotation and satellite view (via Topo/Satellite tile sources).

## Proposed Changes

### 1. Dependencies & Configuration

#### [MODIFY] [libs.versions.toml](file:///E:/Project App Juken/Juken5LiveMonitor/gradle/libs.versions.toml)
- Add `osmdroid = "6.1.18"` to `[versions]`.
- Add `osmdroid = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }` to `[libraries]`.

#### [MODIFY] [build.gradle.kts (app)](file:///E:/Project App Juken/Juken5LiveMonitor/app/build.gradle.kts)
- Add `implementation(libs.osmdroid)` to dependencies.
- (Optional) Remove Google Maps dependencies once transition is complete.

### 2. Map Implementation

#### [MODIFY] [MapPanel.kt](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/components/MapPanel.kt) [REPLACE]
- Replace `GoogleMap` with `osmdroid`'s `MapView` inside an `AndroidView`.
- **Initialization:** Initialize osmdroid configuration in a `LaunchedEffect`.
- **Auto-Rotation:** Implement rotation logic using `mapView.mapOrientation = -bearing`.
- **Controls:** Replace Map/Satellite toggle with OSM Tile Source switching (e.g., MAPNIK vs. USGS Satellite).
- **Recenter:** Implement a custom recenter logic for the `MapView`.

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- Update `mapType` state to use a custom enum or OSM tile source identifier instead of Google's `MapType`.

### 3. Cleanup

#### [MODIFY] [AndroidManifest.xml](file:///E:/Project App Juken/Juken5LiveMonitor/app/src/main/AndroidManifest.xml)
- Remove the Google Maps API Key meta-data.

## Verification Plan

### Automated Tests
- Run Gradle Sync to ensure `osmdroid` is downloaded correctly.
- Build the app and verify no compilation errors.

### Manual Verification
- **Immediate Loading:** Confirm the map loads instantly without "blank/beige" screens.
- **Auto-Follow:** Verify the map centers and rotates as you move.
- **Tile Switching:** Test the "Layers" button to switch between default and satellite/terrain views.
- **Offline Cache:** Ensure tiles are being cached for smoother performance.
