# Fix Gradle Sync Error: Plugin already on classpath with unknown version

The project is failing to sync because the `org.jetbrains.kotlin.android` plugin is being applied in the `:app` module with a version, but it is already present on the Gradle classpath (likely as a dependency of the `kotlin-compose` plugin) without a version being explicitly managed at the root level.

## Proposed Changes

### Root Project

#### [MODIFY] [build.gradle.kts](file:///E:/Project App Juken/Juken5LiveMonitor/build.gradle.kts)
- Add `alias(libs.plugins.kotlin.android) apply false` to the `plugins` block. This ensures the Kotlin plugin version is explicitly defined and managed at the root level, even if it's not applied to the root project itself.

## Verification Plan

### Automated Tests
- Run Gradle Sync (via `gradle_sync` tool or observing the result of a build).
- Run `./gradlew help` to ensure the build script evaluates correctly.

### Manual Verification
- Verify that the error "The request for this plugin could not be satisfied because the plugin is already on the classpath with an unknown version" no longer appears in the Build tool window.
