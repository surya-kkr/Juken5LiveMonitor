# Phase 3: High-Precision Touch Mapping & Viewport Calibration

This phase implements a mathematically precise touch transformation system for Android Auto, ensuring that user interactions are accurate across different themes and screen sizes.

## 1. Core Logic & Transformation

### [NEW] [ViewportCalculator.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/aap/utils/ViewportCalculator.kt)
- Computes the active drawing area within the `SurfaceView`.
- Calculates **ScaleX/Y** (Ratio of AA Viewport to physical pixels).
- Calculates **OffsetX/Y** (Handling Letterbox/Pillarbox offsets).
- Determines **Safe Touch Region**: Bounds where touch is valid for AA.

### [NEW] [TouchTransformEngine.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/aap/utils/TouchTransformEngine.kt)
- Transforms `MotionEvent` coordinates into AA logical coordinates (typically 800x480).
- Uses a transformation matrix for linear and consistent mapping.
- Efficiently handles multi-touch pointer data without new object allocations in the `MOVE` loop.

## 2. Protocol Integration

### [NEW] [TouchEvent.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/aap/protocol/messages/TouchEvent.kt)
- Port the `TouchEvent` protocol message from reference.
- Packs transformed coordinates into the `InputReport` Protobuf message.

### [MODIFY] [AndroidAutoManager.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/aap/manager/AndroidAutoManager.kt)
- Add `sendTouch(event: MotionEvent)`:
    1. Check if `isRunning()`.
    2. Pass event through `TouchTransformEngine`.
    3. Forward to `AapTransport.send()`.

## 3. UI Implementation

### [MODIFY] [AndroidAutoPanel.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/center/androidauto/AndroidAutoPanel.kt)
- Implement `OnTouchListener` on the `SurfaceView`.
- **Debug Overlay**: A Compose `Canvas` layer (Debug Build only) that draws:
    - Crosshair at current touch point.
    - Viewport boundaries (Green for active, Red for letterbox).
    - Live coordinate labels (Android vs AA).
    - Error metrics (X/Y delta).

## 4. Performance & Compatibility

- **Latency**: Logic optimized to complete in < 2ms (Total latency target < 16ms).
- **GC Pressure**: Reuse `ArrayList` and `Triple` structures for touch pointer data.
- **Android 10-15**: Uses standard `MotionEvent` APIs and `Window.OnFrameMetricsAvailableListener` for calibration timing.

## Verification Plan

### Automated
- Unit test for `TouchTransformEngine` validating corner-to-corner mapping.

### Manual
- **100x Loop**: Rapidly switch themes (Classic ↔ Modern) while tapping the launcher button.
- **Gesture Stress**: Persistent 30-minute interactive session with live ECU data.
- **Debug Audit**: Confirm zero touch events are sent to AA when clicking in the letterbox area.
