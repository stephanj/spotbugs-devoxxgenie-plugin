# IntelliJ SpotBugs plugin

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

### How It Works

The plugin detects whether DevoxxGenie is installed at runtime. When available, it constructs a detailed prompt containing the bug pattern, category, rank, source location, and relevant annotations, then sends it to DevoxxGenie's `ExternalPromptService` API. No additional configuration is required — just install both plugins and use them together.

### Requirements

- [DevoxxGenie](https://plugins.jetbrains.com/plugin/24169-devoxxgenie) plugin installed and enabled in IntelliJ IDEA
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
