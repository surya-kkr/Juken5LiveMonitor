# Fix Gradle Plugin Version Conflict

The project is failing to sync because the `org.jetbrains.kotlin.android` plugin is being applied in the `:app` module with a version, but it wasn't managed at the root level. Since other Kotlin-related plugins (like `kotlin-compose`) are declared in the root, Gradle encounters a conflict or ambiguity regarding the version of the core Kotlin plugin.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts (root)](file:///E:/Project App Juken/Juken5LiveMonitor/build.gradle.kts)
- Add `alias(libs.plugins.kotlin.android) apply false` to the `plugins` block. This ensures the Kotlin plugin version is managed at the root and shared across all modules, resolving the "unknown version" error.

## Verification Plan

### Automated Tests
- Run Gradle Sync to verify that the "unknown version" error is resolved and the project syncs successfully.
- Run `./gradlew assembleDebug` (if possible/requested) to ensure the build completes.

### Manual Verification
- Confirm that the `app` module correctly recognizes the Kotlin plugin and and there are no more plugin resolution errors.
