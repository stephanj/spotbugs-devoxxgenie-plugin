/*
 * Copyright 2020 SpotBugs plugin contributors
 *
 * This file is part of IntelliJ SpotBugs plugin.
 *
 * IntelliJ SpotBugs plugin is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * IntelliJ SpotBugs plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IntelliJ SpotBugs plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.jetbrains.plugins.spotbugs.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.ToolWindow;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.spotbugs.core.FindBugsState;
import org.jetbrains.plugins.spotbugs.devoxxgenie.BugSelectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bulk-creates {@code backlog/tasks/*.md} files (DevoxxGenie CLI Runner format) for
 * every bug instance the user has checked in the SpotBugs bug tree.
 */
public final class CreateDevoxxGenieTasksAction extends AbstractAction {

    @Override
    void updateImpl(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ToolWindow toolWindow,
            @NotNull FindBugsState state
    ) {
        BugSelectionManager selectionManager = project.getService(BugSelectionManager.class);
        int count = selectionManager != null ? selectionManager.getSelectedCount() : 0;
        e.getPresentation().setEnabled(count > 0);
        e.getPresentation().setVisible(true);
        if (count > 0) {
            e.getPresentation().setText("Create " + count + " DevoxxGenie Task(s)");
        } else {
            e.getPresentation().setText("Create DevoxxGenie Task(s)");
        }
    }

    @Override
    void actionPerformedImpl(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ToolWindow toolWindow,
            @NotNull FindBugsState state
    ) {
        BugSelectionManager selectionManager = project.getService(BugSelectionManager.class);
        if (selectionManager == null || selectionManager.getSelectedCount() == 0) {
            return;
        }

        // Snapshot selection before starting background task
        Set<BugInstance> selectedBugs = selectionManager.getSelectedBugs();
        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        new Task.Backgroundable(project, "Creating DevoxxGenie Tasks", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    Path backlogDir = Paths.get(basePath, "backlog");
                    Path tasksDir = backlogDir.resolve("tasks");
                    Files.createDirectories(tasksDir);

                    int nextTaskNumber = computeNextTaskNumber(backlogDir);
                    int offset = 0;

                    for (BugInstance bugInstance : selectedBugs) {
                        indicator.setText("Creating task " + (offset + 1) + " of " + selectedBugs.size());
                        createTaskFile(bugInstance, tasksDir, nextTaskNumber + offset);
                        offset++;
                    }

                    LocalFileSystem.getInstance().refreshAndFindFileByPath(tasksDir.toString());
                    selectionManager.clear();

                    int created = offset;
                    Notifications.Bus.notify(
                            new Notification(
                                    "SpotBugs.AnalysisFinished",
                                    "DevoxxGenie Tasks Created",
                                    "Created " + created + " task file(s) in backlog/tasks/",
                                    NotificationType.INFORMATION
                            ),
                            project
                    );
                } catch (IOException ex) {
                    Notifications.Bus.notify(
                            new Notification(
                                    "SpotBugs.AnalyzeError",
                                    "DevoxxGenie Task Creation Failed",
                                    "Failed to create task files: " + ex.getMessage(),
                                    NotificationType.ERROR
                            ),
                            project
                    );
                }
            }
        }.queue();
    }

    private int computeNextTaskNumber(Path backlogDir) {
        int max = 0;
        String[] subdirs = {"tasks", "completed", "archive/tasks"};
        for (String subdir : subdirs) {
            Path dir = backlogDir.resolve(subdir);
            if (Files.exists(dir)) {
                try (var stream = Files.list(dir)) {
                    List<Path> mdFiles = stream
                            .filter(p -> p.toString().endsWith(".md"))
                            .collect(Collectors.toList());
                    for (Path mdFile : mdFiles) {
                        int taskNum = extractTaskNumber(mdFile);
                        if (taskNum > max) {
                            max = taskNum;
                        }
                    }
                } catch (IOException ignored) {
                    // skip unreadable directories
                }
            }
        }
        return max + 1;
    }

    private int extractTaskNumber(Path mdFile) {
        try {
            List<String> lines = Files.readAllLines(mdFile);
            boolean inFrontmatter = false;
            for (String line : lines) {
                if ("---".equals(line.trim())) {
                    if (!inFrontmatter) {
                        inFrontmatter = true;
                    } else {
                        break;
                    }
                    continue;
                }
                if (inFrontmatter && line.startsWith("id:")) {
                    String idStr = line.substring(3).trim();
                    if (idStr.startsWith("TASK-")) {
                        try {
                            return Integer.parseInt(idStr.substring(5));
                        } catch (NumberFormatException ignored) {
                            // not a numeric task id
                        }
                    }
                }
            }
        } catch (IOException ignored) {
            // skip unreadable files
        }
        return 0;
    }

    private void createTaskFile(BugInstance bugInstance, Path tasksDir, int taskNumber) throws IOException {
        String bugType = bugInstance.getBugPattern().getType();
        String category = bugInstance.getBugPattern().getCategory();
        String categoryDesc = I18N.instance().getBugCategoryDescription(category);
        String message = bugInstance.getAbridgedMessage();

        String sourcePath = "";
        String sourceFile = "";
        int startLine = 0;
        try {
            SourceLineAnnotation sourceLine = bugInstance.getPrimarySourceLineAnnotation();
            if (sourceLine != null) {
                sourcePath = sourceLine.getSourcePath();
                sourceFile = sourceLine.getSourceFile();
                startLine = sourceLine.getStartLine();
            }
        } catch (Exception ignored) {
            // use empty defaults
        }

        // Priority mapping: 1=high, 2=medium, 3/4=low
        String priority;
        int priorityInt = bugInstance.getPriority();
        if (priorityInt == 1) {
            priority = "high";
        } else if (priorityInt == 2) {
            priority = "medium";
        } else {
            priority = "low";
        }
        String priorityLabel = bugInstance.getPriorityTypeString();

        String taskId = "TASK-" + taskNumber;
        String today = LocalDate.now().toString();

        String simpleFileName = sourceFile.isEmpty() ? "unknown"
                : sourceFile.endsWith(".java") ? sourceFile.substring(0, sourceFile.length() - 5) : sourceFile;

        String baseNameRaw = "TASK-" + taskNumber + "-spotbugs-" + sanitize(bugType)
                + "-" + sanitize(simpleFileName) + "-l" + startLine;
        String baseName = baseNameRaw.length() > 77 ? baseNameRaw.substring(0, 77) : baseNameRaw;
        String fileName = baseName + ".md";

        String content = "---\n"
                + "id: " + taskId + "\n"
                + "title: Fix " + bugType + " in " + sourceFile + " at line " + startLine + "\n"
                + "status: To Do\n"
                + "priority: " + priority + "\n"
                + "type: bug\n"
                + "assignee: \"\"\n"
                + "labels:\n"
                + "  - spotbugs\n"
                + "  - " + category.toLowerCase() + "\n"
                + "created: " + today + "\n"
                + "---\n"
                + "# Fix `" + bugType + "`: " + message + "\n\n"
                + "## Description\n\n"
                + "- **Rule:** `" + bugType + "`\n"
                + "- **Category:** " + categoryDesc + "\n"
                + "- **File:** `" + sourcePath + "`\n"
                + "- **Line:** " + startLine + "\n"
                + "- **Priority:** " + priorityLabel + "\n\n"
                + "## Acceptance Criteria\n\n"
                + "- [ ] The `" + bugType + "` issue is resolved\n"
                + "- [ ] No new SpotBugs warnings are introduced\n"
                + "- [ ] Existing tests continue to pass\n\n"
                + "## References\n\n"
                + "- [SpotBugs documentation for " + bugType + "](https://spotbugs.readthedocs.io/)\n";

        Files.writeString(tasksDir.resolve(fileName), content);
    }

    private String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9_-]", "-");
    }
}
