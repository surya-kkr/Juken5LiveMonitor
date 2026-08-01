# Android Auto "Self Mode" Integration Plan

This plan outlines the integration of Android Auto Self Mode from the `headunit-revived` reference project into the Juken 5 Live Monitor app.

## User Review Required

> [!IMPORTANT]
> This is a complex integration involving native code (C++), low-level protocol handling (Protobuf/SSL), and background service management.
>
> **Key constraints follow:**
> - Android Auto only runs in **Classic** and **Modern** themes.
> - Full resource release (Stop projection, release decoder, unbind service) when switching to **Racing** or **KKRXAG**.
> - Optimized for **Unisoc T616** using `SurfaceView` and hardware acceleration.

## Proposed Changes

### Build Configuration & Dependencies

#### [MODIFY] [build.gradle.kts (App)](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/build.gradle.kts)
- Add Protobuf plugin and configuration.
- Add CMake/NDK configuration for native components (FFMPEG decoder).
- Add dependencies: `conscrypt-android`, `protobuf-java`, `androidx.media`, `glide`.

#### [NEW] [Native Components](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/cpp)
- Migrate `CMakeLists.txt`, `ffmpeg_hevc_decoder.cpp`, and necessary headers.
- Bring over pre-compiled `.so` files into `jniLibs`.

### Core Android Auto Engine (Internal)

#### [NEW] `com.kkr.juken5livemonitor.aap`
- Migrate the AAP (Android Auto Protocol) implementation:
    - **Protocol**: Protobuf message definitions.
    - **Connection**: `CommManager` for socket handling.
    - **Decoder**: `VideoDecoder` and `AudioDecoder` (extracted and adapted for Unisoc T616).
    - **Service**: `AapService` adapted to run as a background engine rather than a standalone UI driver.

### Manager & Integration

#### [NEW] [AndroidAutoManager.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/aap/AndroidAutoManager.kt)
- The primary controller for the AA lifecycle.
- **`initialize()`**: Setup SSL providers and initial engine state.
- **`start()`**: Start `WirelessServer` and trigger Self Mode intent.
- **`stop()`**: Full teardown of projection, decoder, and connection.
- **`resize(width, height, dpi)`**: Dynamically update viewport for theme transitions.

#### [NEW] [AndroidAutoPanel.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/androidauto/AndroidAutoPanel.kt)
- A Compose component wrapping a `SurfaceView`.
- Connects the `Surface` to the `VideoDecoder` via `AndroidAutoManager`.
- Handles touch event mapping from the panel to the AA protocol.

### Theme Integration

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- Observe `selectedUi` and call `AndroidAutoManager.start()` or `stop()` accordingly.

#### [MODIFY] [ModernCentralDashboard.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/modern/ModernCentralDashboard.kt)
- Replace placeholder with `AndroidAutoPanel`.

#### [MODIFY] [MonitorLandscape.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/MonitorLandscape.kt)
- Integrate `AndroidAutoPanel` into the Classic theme section.

## Verification Plan

### Automated Tests
- Build verification: Ensure CMake and Protobuf compile correctly.

### Manual Verification
- **Theme Switching**:
    - Select **Modern**: Verify AA starts.
    - Switch to **Classic**: Verify AA reloads/resizes without restart.
    - Switch to **Racing**: Verify AA process and resources are completely cleared.
- **Performance**:
    - Monitor CPU/RAM usage on Unisoc T616 during AA session.
    - Check for frame drops or ANRs during data-heavy ECU monitoring.
