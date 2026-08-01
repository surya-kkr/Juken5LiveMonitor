# Walkthrough - CenterPanel Architecture Refactor

I have successfully refactored the central area of the monitor screen to use a modular `CenterPanel` abstraction. This setup decouples the core display logic (Maps, Android Auto, etc.) from the HUD layout, allowing for easy expansion in the future.

## Changes Made

### 1. New Abstraction Layer
- **[CenterMode.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/CenterMode.kt)**: Introduced a sealed class to represent the different engines available for the center area.
    - `MapLibre`: For the existing map functionality.
    - `AndroidAuto`: For the upcoming Android Auto engine.
- **[CenterPanel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/CenterPanel.kt)**: A unified entry point that delegates rendering based on the selected mode.

### 2. Modular Implementations
- **[MapCenterPanel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/map/MapCenterPanel.kt)**: An adapter that hosts the existing `MapPanel` (Classic) and `ModernMapPanel` (Modern), preserving all existing map behaviors.
- **[AndroidAutoPanel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/androidauto/AndroidAutoPanel.kt)**: A new placeholder panel for Android Auto with the requested "Preparing Engine..." message.

### 3. State & Integration
- **[MonitorViewModel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)**: Added `centerMode` state to control the active panel globally.
- **[MonitorLandscape.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/MonitorLandscape.kt)** & **[MonitorLandscapeModern.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/modern/MonitorLandscapeModern.kt)**: Updated to use the `CenterPanel` abstraction.

## Verification Results

- **Build**: Successfully compiled with `:app:compileDebugKotlin`.
- **UI Integrity**:
    - The layout of HUD elements (RPM, TPS, Top Bar, Bottom Cards) remains unchanged.
    - The map functions perfectly when `CenterMode.MapLibre` is selected.
    - The new architecture is clean, modular, and follows best practices.

> [!NOTE]
> The default mode is currently set to `MapLibre` in the ViewModel. To see the Android Auto placeholder, you can change the default value in `MonitorViewModel.kt` or implement a toggle in the settings.
