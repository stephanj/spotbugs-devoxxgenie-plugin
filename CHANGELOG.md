# Changelog

All notable changes to the SpotBugs with DevoxxGenie plugin are documented in this file.

## [1.3.2] - 2026-06-15

### Fixed
- **`Unsupported class file major version 69` when analyzing Java 25 code** — SpotBugs bundled ASM 9.7.1, which only reads up to Java 24 bytecode (major version 68). Bumped `com.github.spotbugs:spotbugs` from 4.9.0 to 4.10.2, which bundles ASM 9.10.1 (reads Java 25). Adapted to the SpotBugs API change `Project.addSourceDir(String)` → `addSourceDirs(Collection<String>)`.
- **Read-access / `WriteIntentReadAction` crashes in the bug preview on IntelliJ 2024.1+** — modern IDEs no longer hold an implicit write-intent read lock on EDT events. `BugTreePanel.setPreview` now wraps its PSI/document read and editor creation in a write-intent read action (acquired reflectively to keep compatibility with the 2023.3 minimum platform), deferring via `invokeLater` when already inside a read action (e.g. a toggle action update).

## [1.3.1] - 2026-02-18

### Added
- **Checkbox multi-select in the bug tree** — click the checkbox icon on any bug row to mark it for bulk processing
- **"Create DevoxxGenie Task(s)" toolbar action** — generates one structured `backlog/tasks/*.md` file per checked bug, compatible with the DevoxxGenie CLI Runner
  - Task files include YAML frontmatter (`id`, `title`, `status`, `priority`, `labels`, `created`) and a Markdown body with rule, category, file, line, and acceptance criteria
  - Task numbering is collision-free: scans `backlog/tasks/`, `backlog/completed/`, and `backlog/archive/tasks/` for existing IDs before assigning the next number
  - Clears the selection and shows a balloon notification after creation

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
