# Task List: Android Auto Integration & Audit

## Phase 1: Infrastructure & Dependencies (Completed)
- [x] Update `libs.versions.toml` with AA dependencies
- [x] Configure `app/build.gradle.kts` (Plugins, NDK, CMake, Dependencies)
- [x] Migrate Native Components and Protobuf Definitions

## Phase 2: AndroidAutoManager & Lifecycle (Completed)
- [x] Define `AndroidAutoState` and `AndroidAutoConfig`
- [x] Implement `AndroidAutoManager` skeleton and State Machine
- [x] Integrate with `MonitorViewModel` and `AndroidAutoPanel`

## Phase 3: Code Audit & Resource Safety (Completed)
- [x] Audit `AndroidAutoManager`: Fix `Context` handling and scope management
- [x] Audit `AapTransport`: Fix `HandlerThread` termination and resource release
- [x] Audit `VideoDecoder`: Fix `MediaCodec` release and thread joining logic
- [x] Audit `WirelessServer`: Tie coroutine lifecycle to Manager scope
- [x] Implement explicit cleanup for Sockets and Surfaces

## Phase 4: Performance Profiling & Optimization (Completed)
- [x] Implement `AapPerformanceMonitor` with `FrameMetrics` listener
- [x] Configure `StrictMode` for AA lifecycle methods
- [x] Add latency tracking to `VideoDecoder`
- [x] Add memory snapshot logging to `AndroidAutoManager`
- [x] Tune `VideoDecoder-Output` thread priorities
- [x] Implement `StressTestUtility` for theme switching loop

## Phase 5: Touch Mapping & Viewport Calibration (Completed)
- [x] Port `TouchEvent` protocol message
- [x] Implement `ViewportCalculator` for precise scaling
- [x] Attach `OnTouchListener` to `AndroidAutoPanel`
- [x] Implement coordinate transformation logic (Double precision)
- [x] Add Debug Overlay for touch visualization
- [x] Support multi-touch and complex gestures
- [x] Verify transition stability (Classic ↔ Modern)
- [x] Write Unit Tests for coordinate transformation (4 passed)

## Phase 5.5: Final Integration Validation (Completed)
- [x] Implement `StateTransitionTest.kt` for State Machine validation
- [x] Add auto-recovery (max 3 retries) in `AndroidAutoManager`
- [x] Enhance logging for AA/Resource monitoring
- [x] Implement `runComprehensiveValidation` in `StressTestUtility`
- [x] Final Resource & Memory Leak Audit
- [x] Certify "Ready for Phase 4"

## Phase 6: High-Performance Audio Subsystem (Completed)
- [x] Implement `AapAudioSink` with zero-allocation buffer pool
- [x] Implement `AapAudioFocusController` with modern API support
- [x] Implement `AapAudioSession` (MediaSessionCompat integration)
- [x] Implement `AapAudioRouter` for dynamic output switching
- [x] Implement `AapAudioManager` orchestrator
- [x] Implement `AapAudioDiagnostics` for debug monitoring
- [x] Integrate Audio Subsystem into `AndroidAutoManager` lifecycle
- [x] Verify resource cleanup on `Racing/KKRXAG` themes

## Phase 7: Release Candidate Hardening (CURRENT)
- [x] Configure `app/build.gradle.kts` for production (Minification, Shrinking)
- [x] Create `proguard-rules.pro` with AA-specific keep rules
- [x] Fix empty catch blocks in `aap` package
- [x] Gate debug features with `BuildConfig.DEBUG`
- [ ] Perform final resource & memory audit on release build
- [ ] Build release APK (`assembleRelease`)
- [ ] Finalize Phase 5 Report and Readiness Score
