# Implementation Plan - Display Log Duration in History

Add functionality to track and display the total duration (minutes and seconds) for each recorded log entry in the History screen.

## User Review Required

> [!CAUTION]
> Changing the database schema will require a database version increment. I will use `fallbackToDestructiveMigration()` which will **delete existing logs** upon the first run after this update. This is the simplest way to apply the schema change during development.

## Proposed Changes

### [Database Layer]

#### [MODIFY] [LogSession.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/data/db/LogSession.kt)
- Add `val durationMillis: Long = 0` to the entity.

#### [MODIFY] [LogDao.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/data/db/LogDao.kt)
- Add `@Query("UPDATE log_sessions SET durationMillis = :duration WHERE id = :id")`
- `suspend fun updateSessionDuration(id: Long, duration: Long)`

#### [MODIFY] [LogDatabase.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/data/db/LogDatabase.kt)
- Increment version to `2`.
- Add `.fallbackToDestructiveMigration()` to the builder.

### [Logic Layer]

#### [MODIFY] [MonitorViewModel.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/viewmodel/MonitorViewModel.kt)
- In `stopRecording()`, get the final duration and call `logDao.updateSessionDuration()`.

### [UI Layer]

#### [MODIFY] [LogHistoryScreen.kt](file:///E:/Project%20App%20Juken/Juken%205%20Live%20Monitor%20App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/log/LogHistoryScreen.kt)
- Update `LogSessionItem` to accept and display the formatted duration (e.g., "Duration: 05:23").

## Verification Plan

### Manual Verification
1. Open the app and start a new recording.
2. Record for a few seconds/minutes.
3. Stop the recording.
4. Go to **VIEW HISTORY LOG**.
5. Verify that the new log entry shows the correct duration in "mm:ss" format.
6. Verify that deleting or renaming still works as expected.
