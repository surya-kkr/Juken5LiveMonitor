# Task: Implement Bluetooth Auto-Reconnect for Live Monitor

- [x] Update `MonitorViewModel.kt`
    - [x] Add `reconnectingStatus` StateFlow
    - [x] Setup disconnect listener in `startMonitor()`
    - [x] Implement `handleDisconnect()` and auto-reconnect logic
    - [x] Refactor `reconnect()` to `reconnectInternal()` for safer retries
- [x] Update `MonitorScreen.kt` (Portrait)
    - [x] Observe `reconnectingStatus`
    - [x] Add "Reconnecting to ECU" popup UI
- [x] Update `MonitorLandscape.kt` (Landscape)
    - [x] Observe `reconnectingStatus`
    - [x] Add "Reconnecting to ECU" popup UI
- [x] Verification
    - [x] Ensure build success
    - [x] Verify logic flow via code analysis
- [x] Finalize with Walkthrough
