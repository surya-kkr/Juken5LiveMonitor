# Implementation Plan - Rearranging Classic Header Layout

Rearrange the components in `ClassicHeader.kt` to move status indicators to the center and telemetry metrics to the right.

## Proposed Changes

### UI Component Refactoring

#### [MODIFY] [ClassicHeader.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/monitor/Landscape/classic/ClassicHeader.kt)
- Reorganize the `Row` content into three main sections:
    - **Left Section**: Bike icon, name, and current time.
    - **Center Section**: Status indicators (GPS, ECU, BT) positioned using weighted spacers.
    - **Right Section**: Speed (km/h) and Fuel information.
- Use `Modifier.weight(1f)` on spacers to ensure the status indicators are perfectly centered.

## Verification Plan

### Manual Verification
- Deploy the app and select the **Classic Android Auto** theme.
- Verify that the GPS, ECU, and BT indicators appear in the center of the header.
- Verify that the Speed (km/h) and Fuel value are moved to the far right.
- Ensure all elements are still properly aligned vertically and not overlapping.
