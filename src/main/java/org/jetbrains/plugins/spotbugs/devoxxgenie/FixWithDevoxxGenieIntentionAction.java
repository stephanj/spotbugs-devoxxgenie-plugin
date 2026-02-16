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
package org.jetbrains.plugins.spotbugs.devoxxgenie;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.spotbugs.common.ExtendedProblemDescriptor;
import org.jetbrains.plugins.spotbugs.intentions.SuppressReportBugIntentionAction;

import javax.swing.Icon;

public class FixWithDevoxxGenieIntentionAction extends SuppressReportBugIntentionAction {

	private final ExtendedProblemDescriptor _descriptor;

	public FixWithDevoxxGenieIntentionAction(ExtendedProblemDescriptor problemDescriptor) {
		super(problemDescriptor);
		_descriptor = problemDescriptor;
	}

	@Override
	@NotNull
	public String getText() {
		return "DevoxxGenie: Fix '" + getBugPatternId() + "'";
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return "DevoxxGenie fix suggestion";
	}

	@Override
	public Icon getIcon(int flags) {
		return AllIcons.Actions.IntentionBulb;
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement context) {
		return DevoxxGenieBridge.isAvailable();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		BugInstance bugInstance = _descriptor.getBug().getInstance();
		PsiFile psiFile = _descriptor.getPsiFile();
		String prompt = DevoxxGeniePromptBuilder.buildPrompt(bugInstance, psiFile);
		DevoxxGenieBridge.sendPrompt(project, prompt);
	}

	@Override
	public boolean startInWriteAction() {
		return false;
	}
}
