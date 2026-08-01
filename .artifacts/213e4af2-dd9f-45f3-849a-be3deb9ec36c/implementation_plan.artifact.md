# Implementation Plan - Telemetry Grid Refactor and Performance Analytics

Refactor the telemetry grid to 2 rows and 10 sections, and implement advanced racing logic including Engine Runtime, GPS stats, and a **Performance HP Estimator** based on the 0-100 km/h timer.

## Proposed Changes

### 1. ViewModel Logic Enhancements
#### [MODIFY] [MonitorViewModel.kt](file:///C:/Users/idas2/OneDrive/Desktop/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- **Engine Runtime**: Starts counting from the first detection of RPM > 0.
- **GPS Stats**: Tracks `topSpeed` and `avgSpeed` cumulative since start.
- **0-100 km/h Timer**:
    - High-precision timer (milliseconds).
    - Status Flow: `IDLE` -> `READY` (Tapped) -> `START` (Speed > 0) -> `FINISHED` (Speed >= 100).
- **Horse Power (HP) Estimation**:
    - Calculated once the 0-100 timer is `FINISHED`.
    - **Formula**: $HP = \text{Weight} \times \left(\frac{\Delta v}{\Delta t}\right) \dots$ (using a standard power-to-weight estimation formula).
    - *Default Weight*: 200kg (Bike + Rider average). We can add this to settings in the future.

### 2. Telemetry Grid Refactor (2 Rows x 5 Columns)
#### [MODIFY] [EliteDashTelemetryGrid.kt](file:///C:/Users/idas2/OneDrive/Desktop/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/racing/EliteDashTelemetryGrid.kt)
- **Row 1**: Battery, RPM, Intake Air Temp, TPS, Ignition.
- **Row 2**: 0-100 (Clickable to Arm), **Horse Power (HP)**, Engine Runtime, Top Speed, Average Speed.
- **Styling**: Maintain Red labels and large White values.

## Verification Plan

### Manual Verification
- Deploy to device.
- Verify sensor mapping in both rows.
- Test 0-100 functionality:
    1. Tap 0-100 box.
    2. Accelerate to 100 km/h.
    3. Verify the final time is displayed.
    4. **Verify the HP box updates** with an estimated value based on that run.
