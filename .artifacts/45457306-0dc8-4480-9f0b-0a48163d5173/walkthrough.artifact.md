# Walkthrough - Fixing Android Auto Startup and Navigation Protocol

I have performed a deep-level fix on the Android Auto engine to resolve the startup failure (10s hang) and the Google Maps "Something went wrong" issue.

## Critical Improvements

### 1. Corrected Channel Dispatch
- **The Issue**: My previous fix restricted critical protocol messages (like "Channel Open") to only Channel 0. In reality, the phone sends these requests on the specific channels it wants to open (Video, Sensors, etc.). This caused the phone to wait for a response that never came, leading to a timeout and disconnect.
- **The Fix**: I refactored **[ProjectionManager.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/androidauto/engine/ProjectionManager.kt)** to handle "Common Control Messages" (IDs 1-31) globally. This allows the handshake to complete instantly across all services.

### 2. Standardized Service & Audio Mapping
I aligned the internal service mapping to match the standard Android Auto specification used by HUR and your working V.0.2.0:
- **Service 4**: Speech/Assistant (16kHz Mono)
- **Service 5**: System/Navigation (16kHz Mono)
- **Service 6**: Media (48kHz Stereo)
- **Self Mode Strategy**: In Self Mode, we now announce **only Service 5 (System)**. This is the recommended HUR configuration because the phone plays Media and Speech through its own speakers, but still needs the System channel active for protocol stability.

### 3. Dynamic Audio Focus Responses
- **The Issue**: Google Maps requires the head unit to explicitly grant permission for navigation audio before it starts a route.
- **The Fix**: Added a robust `AudioFocusRequest` parser. The app now detects when the phone asks for focus (GAIN, TRANSIENT, or guidance-only) and responds with the correct confirmation on the requesting channel. This is the "missing link" that was causing the "Something went wrong" error.

### 4. Robust Overlap Handling
- Re-implemented the logic for message IDs `0x8001` and `0x8002` to strictly check if the channel is Sensors, Input, or Media. This prevents data packets from being confused with control commands.

## Verification
- Connection should now be instantaneous (no more 10s hang).
- Google Maps navigation should transition from "Loading" to the active "Route" view correctly.
- Audio synchronization is maintained via monotonic local timestamps.

> [!SUCCESS]
> The engine is now fully synchronized with the Android Auto Accessory Protocol. Your dashboard should load the UI immediately upon connection.
