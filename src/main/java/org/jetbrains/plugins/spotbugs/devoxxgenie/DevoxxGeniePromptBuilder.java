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

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.spotbugs.common.util.BugInstanceUtil;

public final class DevoxxGeniePromptBuilder {

	private static final int CONTEXT_LINES = 10;

	private DevoxxGeniePromptBuilder() {
	}

	@NotNull
	public static String buildPrompt(@NotNull BugInstance bugInstance, @Nullable PsiFile psiFile) {
		StringBuilder sb = new StringBuilder();
		sb.append("Fix the following SpotBugs issue in my code.\n\n");

		sb.append("**Bug:** ").append(BugInstanceUtil.getBugPatternShortDescription(bugInstance))
				.append(" (`").append(BugInstanceUtil.getBugType(bugInstance)).append("`)\n");

		sb.append("**Issue:** ").append(bugInstance.getAbridgedMessage()).append("\n");

		sb.append("**Category:** ").append(BugInstanceUtil.getBugCategoryDescription(bugInstance)).append("\n");

		sb.append("**Priority:** ").append(BugInstanceUtil.getPriorityTypeString(bugInstance)).append("\n");

		if (psiFile != null) {
			VirtualFile vf = psiFile.getVirtualFile();
			if (vf != null) {
				sb.append("**File:** `").append(vf.getPath()).append("`\n");
			}
		}

		int[] lines = BugInstanceUtil.getSourceLines(bugInstance);
		if (lines[0] > 0) {
			sb.append("**Line:** ").append(lines[0]);
			if (lines[1] != lines[0]) {
				sb.append("-").append(lines[1]);
			}
			sb.append("\n");
		}

		MethodAnnotation method = BugInstanceUtil.getPrimaryMethod(bugInstance);
		if (method != null) {
			sb.append("**Method:** `").append(method.getMethodName()).append("`\n");
		}

		FieldAnnotation field = BugInstanceUtil.getPrimaryField(bugInstance);
		if (field != null) {
			sb.append("**Field:** `").append(field.getFieldName()).append("`\n");
		}

		if (psiFile != null && lines[0] > 0) {
			String codeSnippet = readCodeContext(psiFile, lines[0], lines[1]);
			if (codeSnippet != null) {
				sb.append("\n**Code context:**\n```java\n").append(codeSnippet).append("\n```\n");
			}
		}

		sb.append("\nPlease suggest a fix for this issue. Explain what the problem is and provide the corrected code.\n");

		return sb.toString();
	}

	@Nullable
	private static String readCodeContext(@NotNull PsiFile psiFile, int startLine, int endLine) {
		return ReadAction.compute(() -> {
			VirtualFile vf = psiFile.getVirtualFile();
			if (vf == null) {
				return null;
			}
			Document document = FileDocumentManager.getInstance().getDocument(vf);
			if (document == null) {
				return null;
			}
			int totalLines = document.getLineCount();
			int contextStart = Math.max(0, startLine - 1 - CONTEXT_LINES);
			int contextEnd = Math.min(totalLines - 1, endLine - 1 + CONTEXT_LINES);

			int startOffset = document.getLineStartOffset(contextStart);
			int endOffset = document.getLineEndOffset(contextEnd);
			return document.getText(new com.intellij.openapi.util.TextRange(startOffset, endOffset));
		});
	}
}
