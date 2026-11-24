# Walkthrough - Bluetooth Auto-Reconnect for Live Monitor

I have implemented an automatic reconnection mechanism that triggers when the Bluetooth connection is lost during a live monitoring session.

## Changes Made

### 1. Reconnection Logic in `MonitorViewModel`
- **`reconnectingStatus`**: A new `StateFlow` to track and display the reconnection progress in the UI.
- **Disconnect Listener**: Added `repository.setOnDisconnectListener` in `startMonitor()`. It triggers `handleDisconnect()` only if the app is actively monitoring and was previously in "Live Connected" state.
- **`handleDisconnect()`**:
    - Runs in a background coroutine loop.
    - Updates `reconnectingStatus` with attempt counts.
    - Automatically retries connection every 3 seconds.
    - Once Bluetooth is re-established, it automatically resumes the ECU live stream (sends `OPEN_LIVE` command and waits for ACK).
- **`reconnectInternal()`**: Refactored the reconnection logic to separate the low-level connection from the "monitoring state" management, allowing retries without terminating the session.

### 2. UI Popups
- **`MonitorScreen` (Portrait)**: Added an `AnimatedVisibility` block that shows a "Reconnecting to ECU" popup when `reconnectingStatus` is active.
- **`MonitorLandscape` (Landscape)**: Added a similar popup for landscape orientation to ensure consistency across all layouts.
- **Visuals**: The popup uses an amber-colored theme (amber `CircularProgressIndicator` and icon) to distinguish it from the initial green synchronization popup.

## Verification Results

### Logic Flow Analysis
1. User enters Live Monitor -> `monitoring` = true, `status` = "Live Connected".
2. Bluetooth disconnects -> `onDisconnect` listener triggers -> `handleDisconnect` starts.
3. UI shows "Reconnecting to ECU (Attempt 1...)".
4. App keeps retrying `reconnectInternal` every 3s.
5. Once device is back, it sends `OPEN_LIVE`.
6. Live data resumes, popup disappears.
7. If user clicks "DISCONNECT", `monitoring` becomes false, loop terminates, and popup is cleared.

render_diffs(file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
render_diffs(file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/MonitorScreen.kt)
render_diffs(file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/MonitorLandscape.kt)
