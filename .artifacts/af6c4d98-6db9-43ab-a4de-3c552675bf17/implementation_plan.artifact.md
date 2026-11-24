# Implementation Plan - Fixing Static Image Freeze and Timeout

The user found that freezes occur when the image is static for ~2 seconds. Logcat shows a `SocketTimeoutException` followed by `Socket Closed`. This happens because when the image is static, the phone stops sending data, causing the blocking `read()` to time out and the app's loop to break.

## User Review Required

> [!IMPORTANT]
> I will increase the socket timeout to 10 seconds (standard in reference app) and, most importantly, modify the loop so it **never exits** on a timeout. Instead, it will send a Ping and keep waiting.

## Proposed Changes

### Component: Android Auto Engine

#### [MODIFY] [ProjectionManager.kt](file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/androidauto/engine/ProjectionManager.kt)
- **Timeout Management**: Increase `socket.soTimeout` from 5,000ms to 10,000ms.
- **Robust Loop**: Update `runReadLoop` to handle `SocketTimeoutException` by continuing the loop instead of breaking.
- **Watchdog Removal**: Remove the aggressive `watchdogJob` that stops the decoder on idle, as static images are valid states in Android Auto.
- **Direct Read Integration**: Re-simplify the loop to use blocking `read()` with timeout tolerance, similar to how the reference app handles it.

## Verification Plan

### Automated Tests
- Build and run the app.
- Ensure "Read Loop Started" appears.

### Manual Verification
- Connect to Android Auto.
- Leave the screen on a static page (e.g., Home or Settings) for more than 5 seconds.
- **Success Criteria**: The connection should NOT drop, and when you move the map or touch the screen, it should respond immediately.
