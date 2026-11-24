# Implementation Plan - Bluetooth Auto-Reconnect for Live Monitor

This plan implements automatic Bluetooth reconnection when the connection is lost while the app is in the Live Monitor screen. It includes a "Reconnecting" popup similar to the initial synchronization popup.

## User Review Required

> [!IMPORTANT]
> The auto-reconnect feature will only trigger if the connection was previously in "Live Connected" state. If the initial connection or synchronization fails, the existing error handling remains.
> Manual disconnection by the user will NOT trigger auto-reconnect.

## Proposed Changes

### 1. ViewModel Layer

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- Add `reconnectingStatus` StateFlow to track the reconnection progress and show/hide the popup.
- Update `startMonitor()` to set up a disconnect listener using `repository.setOnDisconnectListener`.
- Implement `handleDisconnect()` logic:
    - Verify if the app is still in monitoring mode and was previously connected.
    - Set `reconnectingStatus` to a non-null value to trigger the UI popup.
    - Start a reconnection loop that attempts to re-establish the Bluetooth connection and resume the live data stream.
    - On successful reconnection, restore listeners and resume heartbeat (ping).
    - On failure, continue retrying until successful or until the user manually disconnects.
- Refactor `reconnect()` into a more granular `reconnectInternal()` to avoid setting `monitoring = false` during auto-retry attempts.

### 2. UI Layer

#### [MODIFY] [MonitorScreen.kt](file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/MonitorScreen.kt)
- Observe `reconnectingStatus` from the `MonitorViewModel`.
- Add an `AnimatedVisibility` block to display the "Reconnecting" popup.
- The popup will follow the design of the "Sync Loading Popup" but with:
    - Different Title: "Reconnecting to ECU"
    - Status Text: Showing the current retry attempt or state.
    - Icon: `Icons.Default.BluetoothSearching` (if available) or `Icons.Default.Sync`.

### 3. Repository / Protocol Layer (If needed)
- Ensure `ECURepository` correctly handles listener re-assignment on the new `BluetoothManager` instance created during reconnection. (Existing `setLiveListener` should be sufficient if called after reconnection).

---

## Verification Plan

### Automated Tests
- N/A (Mainly hardware/interaction dependent).

### Manual Verification
1. **Normal Flow**: Open Live Monitor, wait for "Live Connected".
2. **Simulated Disconnect**: Power off the Bluetooth device or move out of range.
3. **Popup Verification**: Verify the "Reconnecting to ECU" popup appears immediately.
4. **Auto-Reconnect**: Power the device back on or move back into range.
5. **Success Verification**: Verify the popup disappears and live data resumes automatically without user intervention.
6. **Manual Disconnect**: Verify that clicking "DISCONNECT" button does NOT trigger the reconnect popup and exits to the previous screen normally.
