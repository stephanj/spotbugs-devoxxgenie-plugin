# IntelliJ SpotBugs plugin — DevoxxGenie Fork

![SpotBugsAndDevoxxGenie](https://github.com/user-attachments/assets/0ee8b5fd-51ca-45af-9465-60db2cda0950)

[![team JetBrains project](https://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
![Build](https://github.com/JetBrains/spotbugs-intellij-plugin/workflows/Build/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/JetBrains/spotbugs-intellij-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/JetBrains/spotbugs-intellij-plugin)

IntelliJ SpotBugs plugin provides static byte code analysis to look for bugs in Java code from within IntelliJ IDEA.
The plugin uses [SpotBugs](https://spotbugs.github.io/) under the hood.

## DevoxxGenie Integration

This fork adds integration with the [DevoxxGenie](https://github.com/devoxx/DevoxxGenieIDEAPlugin) IntelliJ plugin, allowing you to send SpotBugs findings directly to an AI assistant for fix suggestions.

### Features

- **Intention action** — When SpotBugs highlights a bug in the editor, use the "DevoxxGenie: Fix '...'" quick-fix from the Alt+Enter menu to send the finding to DevoxxGenie.
- **Line marker integration** — Right-click a SpotBugs gutter icon to access the DevoxxGenie fix action alongside the existing suppress/clear options.
- **Bug details panel button** — A "Fix with DevoxxGenie" button appears in the SpotBugs tool window when viewing bug details.
- **Bulk task creation** — Select multiple bugs in the tree and generate structured task files for the DevoxxGenie CLI Runner in one click.

<img width="2056" height="1329" alt="Screenshot 2026-02-16 at 12 37 06" src="https://github.com/user-attachments/assets/a69e08cd-7c93-402a-a283-f4afaaab93d3" />

### Bulk Task Creation

After running an analysis, you can create [DevoxxGenie CLI Runner](https://github.com/devoxx/DevoxxGenieIDEAPlugin) task files for multiple bugs at once:

1. **Check bugs in the tree** — click the checkbox icon that appears to the left of any bug row in the SpotBugs tool window. Checked bugs are highlighted with a filled checkbox; click again to deselect.
2. **Click "Create N DevoxxGenie Task(s)"** in the tool window toolbar (the button label updates to show the current selection count).
3. **Task files are written** to `backlog/tasks/` in your project root, one `.md` file per bug.

Each generated file follows the DevoxxGenie CLI Runner format with YAML frontmatter and a Markdown body:

```markdown
---
id: TASK-42
title: Fix NULL_DEREFERENCE in MyService.java at line 87
status: To Do
priority: high
type: bug
assignee: ""
labels:
  - spotbugs
  - correctness
created: 2026-02-18
---
# Fix `NULL_DEREFERENCE`: Null pointer dereference of ...

## Description

- **Rule:** `NULL_DEREFERENCE`
- **Category:** Correctness
- **File:** `org/example/MyService.java`
- **Line:** 87
- **Priority:** High

## Acceptance Criteria

- [ ] The `NULL_DEREFERENCE` issue is resolved
- [ ] No new SpotBugs warnings are introduced
- [ ] Existing tests continue to pass

## References

- [SpotBugs documentation for NULL_DEREFERENCE](https://spotbugs.readthedocs.io/)
```

Task numbers are assigned without collisions — the plugin scans `backlog/tasks/`, `backlog/completed/`, and `backlog/archive/tasks/` for existing `id: TASK-N` entries and picks the next available number. After creation, the selection is cleared and a notification confirms how many files were written.

### How It Works

The plugin detects whether DevoxxGenie is installed at runtime. When available, it constructs a detailed prompt containing the bug pattern, category, rank, source location, and relevant annotations, then sends it to DevoxxGenie's `ExternalPromptService` API. No additional configuration is required — just install both plugins and use them together.

### Requirements

- [DevoxxGenie](https://plugins.jetbrains.com/plugin/24169-devoxxgenie) plugin installed (v0.9.12 or higher) and enabled in IntelliJ IDEA
- SpotBugs analysis results available (run an analysis first)

---

The plugin is created by [Andrey Cherkasov](mailto:jqy@protonmail.com) and sponsored by JetBrains s.r.o.

The plugin is based on [FindBugs-IDEA](https://github.com/andrepdo/findbugs-idea), which is created by [Andre Pfeiler](mailto:andrepdo@dev.java.net) and licensed under the [GNU LESSER GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html).

Contribution from [Stanislav Anokhin](mailto:staslock@gmail.com) (migration to gradle build system) is used.

[SpotBugs](https://spotbugs.github.io/) is the spiritual successor of [FindBugs](https://github.com/findbugsproject/findbugs), carrying on from the point where it left off with support of its community.

SpotBugs is licensed under the [GNU LESSER GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html).

### FindBugs trademark and licenses

FindBugs Copyright  2003-2015 University of Maryland and others. 
The FindBugs name and logo are trademarked by the University of Maryland.
Findbugs is written and maintained by the FindBugs development team, 
with help from numerous contributors. GUI2 (which you are now using) 
was written by University of Maryland undergraduates Daniel Hakim, 
Reuven Lazarus and Kristin Stephens as part of the FindBugs Summer of Code 2006, 
supported by Sun Microsystems.

FindBugs is sponsored by Fortify Software.

Visit the FindBugs web page for more information at https://findbugs.sourceforge.net
FindBugs is free software; see the License tab for details.

This product includes software developed at
[The Apache Software Foundation](https://www.apache.org/).

Gui Resource Icons Copyright:
Most Icons are taken from IntelliJ IDEA with slightly modifications.
