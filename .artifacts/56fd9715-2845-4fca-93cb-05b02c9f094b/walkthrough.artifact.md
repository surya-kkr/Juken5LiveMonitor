# Walkthrough - Data Logging and Playback System

I have successfully implemented the Data Logging system. This feature allows users to record live ECU data, save it to a local database, and play it back later with a seek bar.

## Key Features

### 1. Real-time Recording
- Added a **Record/Stop** button in the Top Bar (Racing & KKRXAG themes).
- When recording is active, the button icon changes to a "Stop" square and the system captures data from all sensors (RPM, TPS, AFR, Battery, Temps, etc.) at a regular interval.
- **Recording Indicator Overlay**: A floating popup now appears at the top center of the screen during recording. It displays "Recording Log" and a real-time counter (minutes:seconds).
- **Auto-Stop Limit**: Added a maximum recording limit of **10 minutes**. The recording will automatically stop and save when this limit is reached to manage storage and database size.

### 2. Log History
- Accessible via **Settings -> VIEW HISTORY LOG**.
- Displays a list of all recorded sessions with titles (Date & Time).
- Each entry has a **Play** button to view the data and a **Delete** button to remove the log.
- **Multi-Selection & Bulk Delete**: Added support for selecting multiple logs.
    - **Long Press**: Press and hold any item to enter selection mode.
    - **Contextual Action Bar**: The header changes to show the number of selected items and a bulk delete button.
    - **Selection Indicators**: Each item shows a checkbox when in selection mode for easy multi-picking.
    - **Bulk Confirmation**: One confirmation dialog handles deleting all selected items at once.
- **Rename Capability**: Users can now rename any log entry.
    - **Edit Icon**: A new pencil icon is available for each item in the history.
    - **Rename Dialog**: Clicking the edit icon opens a dialog with a text field to enter a new name for the log.
- **Duration Display**: The history list now shows the total duration of each log in `mm:ss` format next to the date.

### 3. Data Playback Screen
- A specialized playback interface showing the recorded sensor values and bars.
- Includes a **Video-style Slider (Seek Bar)** to navigate to any point in the recorded timeline.
- **Play/Pause** button to control the playback flow.
- Shows time elapsed and current frame index.

## Technical Details

### Database (Room)
- **LogSession**: Stores the metadata for each recording session.
- **LogEntry**: Stores the actual sensor data points linked to a session.

### ViewModel
- **MonitorViewModel**: Handles the recording state and background data capture.
- **LogViewModel**: Manages fetching and deleting logs from the database.

### 4. Build Fix (Kotlin 2.1 Compatibility)
- Upgraded Room to **2.8.4** to support Kotlin 2.1.21 metadata.
- Migrated Room annotation processing from **kapt** to **KSP** as required for newer Room versions and better Kotlin support.

## Verification

- [x] Recording starts and saves entries to the database.
- [x] Logs are persisted and can be listed in the History screen.
- [x] Playback correctly replays the data sequence.
- [x] Seek bar allows jumping to specific timestamps in the log.
- [x] Delete functionality removes both session and linked entries (Cascade).

> [!TIP]
> Use the **Demo Mode** to quickly test the recording and playback without needing a real ECU connection.
