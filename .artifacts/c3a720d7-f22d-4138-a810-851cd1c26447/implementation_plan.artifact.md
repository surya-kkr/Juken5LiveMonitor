# Implementation Plan - ECU Bluetooth Stability Fix (Xiaomi/Redmi/Vivo)

This plan targets the specific connection issues between the ECU and the app on Xiaomi, Redmi, and Vivo devices. It focuses on making the Bluetooth stack more resilient to aggressive battery management and strict security policies found in MIUI and OriginOS.

## User Review Required

> [!IMPORTANT]
> - **Insecure Connection Fallback**: I will modify the app to try an "Insecure" Bluetooth connection if the standard one fails. This is often required for Xiaomi and Vivo phones to talk to external hardware like ECUs.
> - **Priority Reading**: I will increase the priority of the data reading thread so the system doesn't "pause" the connection when you switch screens or when the phone gets busy.
> - **Connection UI**: If the connection fails, the app will provide more specific logs to help us identify if it's a permission issue or a hardware timeout.

## Proposed Changes

### [app] Bluetooth Manager Upgrade

#### [MODIFY] [BluetoothManager.kt](file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/bluetooth/BluetoothManager.kt)
- **Fallback Logic**: Implement a retry mechanism.
    - Attempt 1: `createRfcommSocketToServiceRecord` (Secure)
    - Attempt 2 (if fail): `createInsecureRfcommSocketToServiceRecord` (Insecure)
- **Discovery Guard**: Add a safety check to ensure `cancelDiscovery()` is fully processed before calling `connect()`.
- **Thread Priority**: Change `thread {}` to use `Thread.MAX_PRIORITY` and `Process.THREAD_PRIORITY_AUDIO` (high priority) to prevent the "loading but not moving" issue caused by thread starvation on Xiaomi.

### [app] Synchronization & Handshake

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- **Handshake Patience**: Increase the initial handshake timeout slightly for Xiaomi devices which take longer to initialize the serial buffer.
- **Buffer Flush**: Add a command to clear the ECU's serial buffer more aggressively before entering "Live Mode".

## Verification Plan

### Manual Verification
1. **Target Devices**: Test connection on **Xiaomi 11T** or **Redmi Note 15 Pro**.
2. **Success Criteria**:
    - App should connect without "Failed to Connect" error.
    - Progress bar should move past "Preparing Live Mode".
    - Dashboard numbers (RPM/TPS) should move smoothly (not stuck).
3. **Regression Test**: Test on **Samsung** or **Infinix** to ensure they still work perfectly.
