# Walkthrough - 5-Second Countdown for 0-100 km/h

I have implemented a visual countdown sequence for the 0-100 km/h measurement to give you time to prepare before the timer starts.

## Changes Made

### 1. Countdown Logic
- **File**: `MonitorViewModel.kt`
- **Feature**: When the 0-100 test is triggered (via tap or ECU pattern), it now enters a `COUNTDOWN` state.
- **Sequence**: It counts down from **5, 4, 3, 2, 1** then flashes **"START!"**.
- **Action**: Only after the countdown finishes does the timer enter the **READY** (Yellow) state, waiting for the bike to move.

### 2. Full-Screen Overlay
- **File**: `MonitorLandscape.kt`
- **Visuals**:
    - **Dimming**: When counting down, the entire dashboard is covered by a black overlay with **50% opacity**.
    - **Large Text**: The numbers are displayed in the exact center of the screen with a massive font size (**120.sp**) and heavy weight for maximum visibility while riding.

## Verification
- **Functionality**: The countdown works for both manual taps on the 0-100 item and the automatic fuel switch pattern (`0-1-0-1`).
- **Safety**: You cannot trigger a new countdown if one is already running or if the timer is already active.
- **Cross-Theme**: The overlay is implemented at the top-level landscape container, so it works perfectly in **Classic**, **Modern**, and **Racing** themes.
