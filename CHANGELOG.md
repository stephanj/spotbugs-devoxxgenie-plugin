# Changelog

All notable changes to the SpotBugs with DevoxxGenie plugin are documented in this file.

## [1.3.0] - 2026-02-16

### Added
- **DevoxxGenie integration** for AI-powered bug fix suggestions:
  - "Fix with DevoxxGenie" button in the bug details panel toolbar (visible when DevoxxGenie is installed)
  - Intention action on all bug annotations and line markers to send findings to DevoxxGenie
  - Constructs detailed prompts from bug instances including bug pattern, category, source location, and annotations
- Kotlin support for newer plugin components

### Changed
- Moved toolbar actions from vertical left sidebar to horizontal header for a cleaner layout
- Renamed plugin to "SpotBugs with DevoxxGenie"
- Upgraded Gradle from 8.12 to 9.3.1 (Java 25 support)
- Upgraded IntelliJ Platform Gradle Plugin from 1.16.1 to 2.11.0
- Upgraded foojay-resolver-convention from 0.8.0 to 0.9.0
- Updated multiple dependencies to latest versions
- Removed `untilBuild` constraint for forward IDE compatibility
- Refactored project open/close listener to use `ProjectActivity` and `Disposable`
- Rewrote notification handling

### Fixed
- Plugin ID constant now matches `plugin.xml` (was causing `IllegalStateException` at runtime)
- Replaced deprecated APIs marked for removal
- Rewrote deprecated `addBrowseFolderListener` API calls
- Resolved threading and null safety issues in tree navigation
- Added JDK and library dependencies to SpotBugs aux classpath
- Deferred `idea.home.path` validation to execution time so non-test tasks can run without it

## [1.2.9]

### Changed
- Bumped SpotBugs version

## [1.2.7]

### Fixed
- Excluded slf4j from plugin distribution to use the one from the platform
- Fixed additional deprecation warnings

## [1.2.6]

### Fixed
- Urgent issues and deprecated API usages
- Updated dependencies
- Restored grouping of bugs by priority
- Null-check in `BugAnnotator`
