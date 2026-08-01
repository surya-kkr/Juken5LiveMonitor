# Implementation Plan - CenterPanel Abstraction Refactor

Refactor the central area of the monitor screen to use a new `CenterPanel` abstraction. This allows switching between different engines (MapLibre, Android Auto placeholder) without modifying the surrounding HUD layout.

## User Review Required

> [!IMPORTANT]
> **No UI Changes**: The layout, positions of RPM/TPS bars, Top Bar, and Bottom Cards will remain exactly as they are.
> **Engine Switch**: I am introducing `CenterMode` (sealed class) and `CenterPanel` (composable) to decouple the UI from specific map implementations.

## Proposed Changes

### [Component: CenterPanel Abstraction]

#### [NEW] [CenterMode.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/CenterMode.kt)
- Define `sealed class CenterMode` with `MapLibre` and `AndroidAuto` objects.

#### [NEW] [CenterPanel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/CenterPanel.kt)
- Create a `CenterPanel` composable that takes `CenterMode` as a parameter.
- Use a `when` expression to delegate rendering to either `MapCenterPanel` or `AndroidAutoPanel`.

### [Component: CenterPanel Implementations]

#### [NEW] [MapCenterPanel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/map/MapCenterPanel.kt)
- This will act as the adapter for the existing map functionality.
- Since there are currently two map styles (`Classic` and `Modern`), this panel will handle the appropriate style based on the current UI settings.

#### [NEW] [AndroidAutoPanel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/androidauto/AndroidAutoPanel.kt)
- Implement the requested placeholder UI for Android Auto:
    ```
    ANDROID AUTO
    Preparing Engine...
    ```

### [Component: ViewModel & Integration]

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- Add `_centerMode` (`MutableStateFlow<CenterMode>`) and its public `asStateFlow()` property.
- Default mode will be `CenterMode.MapLibre`.

#### [MODIFY] [MonitorLandscape.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/MonitorLandscape.kt)
- Collect `centerMode` from the ViewModel.
- Replace the direct call to `MapPanel` with `CenterPanel`.

#### [MODIFY] [MonitorLandscapeModern.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/modern/MonitorLandscapeModern.kt)
- Collect `centerMode` from the ViewModel.
- Replace the direct call to `ModernMapPanel` with `CenterPanel`.

## Verification Plan

### Automated Tests
- Run `:app:compileDebugKotlin` to ensure all references are correct and the build passes.

### Manual Verification
- Verify that the map still appears and functions exactly as before when in `MapLibre` mode.
- (Optional/Future) Switch to `AndroidAuto` mode (e.g., via a debugger or temporary toggle) and verify the placeholder is displayed correctly within the center area.
