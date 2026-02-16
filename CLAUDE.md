# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IntelliJ SpotBugs plugin — provides static bytecode analysis for Java from within IntelliJ IDEA using SpotBugs. Maintained by JetBrains. Licensed under GNU LGPL 2.1.

- Plugin ID: `org.jetbrains.plugins.spotbugs`
- Min IntelliJ version: 2023.3 (since-build 222.1)
- Java 17 (via `jvmToolchain(17)`)
- Kotlin 2.1.10, Gradle 8.12, IntelliJ Platform Gradle Plugin 2.2.1

## Build Commands

```bash
./gradlew build                  # Full build (compile + test + package)
./gradlew buildPlugin            # Build plugin distribution zip
./gradlew runIde                 # Launch IntelliJ with plugin loaded
./gradlew test                   # Run tests (requires IDEA_HOME_PATH)
./gradlew jacocoTestReport       # Generate code coverage (runs tests first)
```

### Test Prerequisite

Tests require the `intellij-community` source checkout. Set **one** of:
- Environment variable: `IDEA_HOME_PATH=~/path/to/intellij-community`
- Gradle property in `gradle.properties`: `idea.home.path=~/path/to/intellij-community`

Tests use JUnit 4 + IntelliJ Platform test framework (`JavaCodeInsightFixtureTestCase`).

### Third-Party Plugin Downloads

`compileJava` automatically triggers `downloadThirdPartyPlugins` (fb-contrib, find-security-bugs) and `copyGradleProperties`. These are placed in `build/resources/main/org/jetbrains/plugins/spotbugs/plugins/`.

## Architecture

### Source Layout

All source is under `src/main/java/org/jetbrains/plugins/spotbugs/` (Java and Kotlin mixed). The codebase is primarily Java with a few Kotlin files for newer components.

### Key Packages

- **`core/`** — Plugin lifecycle and analysis orchestration. `FindBugsStarter` is the abstract base that drives analysis: handles compilation, configures SpotBugs `FindBugs2` engine, manages progress, and publishes results via the message bus. Settings hierarchy: `AbstractSettings` → `ProjectSettings`, `ModuleSettings`, `WorkspaceSettings`.
- **`messages/`** — Event-driven communication via IntelliJ's message bus. Topics: `AnalysisStartedListener`, `AnalysisFinishedListener`, `AnalysisAbortingListener`, `AnalysisAbortedListener`, `NewBugListener`, `ClearListener`. `MessageBusManager` is the central dispatcher.
- **`actions/`** — All toolbar/menu actions (analyze selected files, module, project, changelist, etc.). Each action extends IntelliJ's `AnAction` and typically creates a concrete `FindBugsStarter` subclass.
- **`gui/`** — UI components organized into subpackages: `editor/` (annotators, line markers), `toolwindow/` (main results panel), `tree/` (result tree views), `settings/` (configuration UI), `preferences/` (legacy settings), `intentions/` (quick fixes), `export/` (HTML/XML export).
- **`plugins/`** — Loading of SpotBugs detector plugins (fb-contrib, find-security-bugs, custom plugins). `PluginLoader` manages the lifecycle.
- **`collectors/`** — `ClassCollector` and `FileCollector` gather files for analysis from the IDE's project model.

### Analysis Flow

1. User triggers an action (e.g., `AnalyzeProjectFilesIncludingTests`)
2. Action creates a `FindBugsStarter` subclass and calls `start()`
3. `FindBugsStarter.start()` optionally compiles first, then runs analysis in a background `Task`
4. `configure()` builds `FindBugsProject` instances per module via collectors
5. `executeImpl()` configures the SpotBugs `FindBugs2` engine with detectors, filters, and user preferences, then calls `engine.execute()`
6. `Reporter` receives bug instances and publishes them via `MessageBusManager`
7. `ToolWindowPanel` listens for results and updates the tree view

### Plugin Registration

`plugin.xml` registers: post-startup activity, tool window, project/module configurables, annotators (Java, Scala, Groovy), line marker providers, checkin handler, notification groups, and all action groups.

### Entry Point

`SpotBugsPostStartupActivity` (Kotlin) — runs on project open, initializes the plugin state.

## Conventions

- Package prefix: `org.jetbrains.plugins.spotbugs`
- Icons defined in `icons/PluginIcons.java`
- i18n strings in `src/main/resources/org/jetbrains/plugins/spotbugs/resources/i18n/Messages.properties`
- Version managed in `gradle.properties` and copied to resources at build time as `version.properties`
- Test data files live in the top-level `test/` directory (not under `src/`)
