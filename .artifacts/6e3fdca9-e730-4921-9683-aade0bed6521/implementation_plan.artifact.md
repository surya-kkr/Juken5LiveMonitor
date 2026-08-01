# Implementation Plan - 0-100 km/h Cancellation & 2-Minute Cooldown

Add a mechanism to cancel an active 0-100 km/h test and enforce a 2-minute cooldown period where the test cannot be restarted.

## User Requirements

- **Trigger**: Clicking the 0-100 km/h button while it is already active (Counting down, Ready, or Running).
- **Action**:
    - Immediately stop the current test.
    - Set a **2-minute (120 seconds) cooldown**.
    - Display "CANCEL" in large text in the center of the screen (using the same overlay as the countdown).
    - Backdrop dimming (50% opacity) should remain active while "CANCEL" is shown.
- **Cooldown**: During these 2 minutes, the 0-100 km/h feature is disabled.

## Proposed Changes

### [Component] Monitor View Model

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- Add `COOLDOWN` to `TimerStatus` enum.
- Add `_cooldownRemaining = MutableStateFlow(0)` to track the time.
- Update `armZeroToHundred()`:
    - Logic to detect if a test is already in progress.
    - If in progress, cancel the active job and call a new `triggerCooldown()` function.
- Implement `triggerCooldown()`:
    - Set status to `COOLDOWN`.
    - Set `countdownValue` to "CANCEL".
    - Delay for 2 seconds (to show the "CANCEL" message).
    - Clear `countdownValue`.
    - Loop for 120 seconds, decrementing `_cooldownRemaining`.
    - Reset status to `IDLE` once finished.

### [Component] Racing UI - Telemetry Grid

#### [MODIFY] [EliteDashTelemetryGrid.kt](file:///E:/Project%20App%20Juken/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/racing/EliteDashTelemetryGrid.kt)
- Update the display value for the 0-100 grid item:
    - If status is `COOLDOWN`, show "WAIT ${remaining}s" or just "WAIT".
    - Adjust the `valueColor` for the cooldown state (e.g., `Color.Gray`).

## Verification Plan

### Manual Verification
- Start a 0-100 km/h test (manual click).
- While the 5-second countdown is running, click the button again.
- Verify that "CANCEL" appears in large letters and the screen is dimmed.
- Verify that the grid item shows a "WAIT" or "COOLDOWN" message.
- Verify that clicking the button during the 2-minute cooldown does nothing.
- Wait 2 minutes and verify the feature becomes available again.
