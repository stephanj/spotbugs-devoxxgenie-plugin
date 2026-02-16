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
package org.jetbrains.plugins.spotbugs.gui.toolwindow.view;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.*;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.spotbugs.common.util.BugInstanceUtil;
import org.jetbrains.plugins.spotbugs.devoxxgenie.DevoxxGenieBridge;
import org.jetbrains.plugins.spotbugs.devoxxgenie.DevoxxGeniePromptBuilder;
import org.jetbrains.plugins.spotbugs.gui.common.*;
import org.jetbrains.plugins.spotbugs.gui.tree.view.BugTree;
import org.jetbrains.plugins.spotbugs.resources.GuiResources;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("MagicNumber")
public final class BugDetailsComponents {

	private static final Logger LOGGER = Logger.getInstance(BugDetailsComponents.class);

	private HTMLEditorKit _htmlEditorKit;
	private JEditorPane _bugDetailsPane;
	private JEditorPane _explanationPane;
	private JPanel _bugDetailsPanel;
	private JPanel _explanationPanel;
	private JButton _fixWithDevoxxGenieButton;
	private final ToolWindowPanel _parent;
	private double _splitPaneHorizontalWeight = 0.6;
	private SortedBugCollection _lastBugCollection;
	private BugInstance _lastBugInstance;
	private MultiSplitPane _bugDetailsSplitPane;


	BugDetailsComponents(final ToolWindowPanel toolWindowPanel) {
		_parent = toolWindowPanel;
		_htmlEditorKit = GuiResources.createHtmlEditorKit();
	}

	Component getBugDetailsSplitPane() {
		if (_bugDetailsSplitPane == null) {
			_bugDetailsSplitPane = new MultiSplitPane();
			_bugDetailsSplitPane.setContinuousLayout(true);
			final String layoutDef = "(ROW weight=1.0 (COLUMN weight=1.0 top bottom))";
			final MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
			final MultiSplitLayout multiSplitLayout = _bugDetailsSplitPane.getMultiSplitLayout();
			multiSplitLayout.setDividerSize(3);
			multiSplitLayout.setModel(modelRoot);
			multiSplitLayout.setFloatingDividers(true);
			_bugDetailsSplitPane.add(getBugDetailsPanel(), "top");
			_bugDetailsSplitPane.add(getBugExplanationPanel(), "bottom");


		}
		return _bugDetailsSplitPane;
	}

	private JPanel getBugDetailsPanel() {
		if (_bugDetailsPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setViewportView(getBugDetailsPane());

			_bugDetailsPanel = new JPanel();
			_bugDetailsPanel.setBorder(JBUI.Borders.empty());
			_bugDetailsPanel.setLayout(new BorderLayout());
			_bugDetailsPanel.add(createToolbarPanel(), BorderLayout.NORTH);
			_bugDetailsPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _bugDetailsPanel;
	}

	private JPanel createToolbarPanel() {
		_fixWithDevoxxGenieButton = new JButton("Fix with DevoxxGenie");
		_fixWithDevoxxGenieButton.setBackground(new Color(0x35, 0x74, 0xF0));
		_fixWithDevoxxGenieButton.setForeground(Color.WHITE);
		_fixWithDevoxxGenieButton.setFocusPainted(false);
		_fixWithDevoxxGenieButton.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0x2B, 0x5F, 0xC3), 1, true),
				JBUI.Borders.empty(4, 12)
		));
		_fixWithDevoxxGenieButton.setOpaque(true);
		_fixWithDevoxxGenieButton.setVisible(false);
		_fixWithDevoxxGenieButton.addActionListener(e -> {
			if (_lastBugInstance != null && _parent != null) {
				final String prompt = DevoxxGeniePromptBuilder.buildPrompt(_lastBugInstance, null);
				DevoxxGenieBridge.sendPrompt(_parent.getProject(), prompt);
			}
		});

		final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		toolbarPanel.add(_fixWithDevoxxGenieButton);
		return toolbarPanel;
	}

	private JEditorPane getBugDetailsPane() {
		if (_bugDetailsPane == null) {
			_bugDetailsPane = new BugDetailsEditorPane();
			_bugDetailsPane.setBorder(JBUI.Borders.empty(5));
			_bugDetailsPane.setEditable(false);
			_bugDetailsPane.setContentType(UIUtil.HTML_MIME);
			_bugDetailsPane.setEditorKit(_htmlEditorKit);
			_bugDetailsPane.addHyperlinkListener(evt -> {
				if (_parent != null) {
					handleDetailsClick(evt);
				}
			});
		}

		return _bugDetailsPane;
	}

	private JPanel getBugExplanationPanel() {
		if (_explanationPanel == null) {
			final JScrollPane scrollPane = ScrollPaneFacade.createScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setViewportView(getExplanationPane());

			_explanationPanel = new JPanel();
			_explanationPanel.setBorder(JBUI.Borders.empty());
			_explanationPanel.setLayout(new BorderLayout());
			_explanationPanel.add(scrollPane, BorderLayout.CENTER);
		}

		return _explanationPanel;
	}

	@SuppressWarnings({"AnonymousInnerClass"})
	private JEditorPane getExplanationPane() {
		if (_explanationPane == null) {
			_explanationPane = new ExplanationEditorPane();
			_explanationPane.setBorder(JBUI.Borders.empty(10));
			_explanationPane.setEditable(false);
			_explanationPane.setContentType("text/html");
			_explanationPane.setEditorKit(_htmlEditorKit);
			_explanationPane.addHyperlinkListener(this::editorPaneHyperlinkUpdate);
		}

		return _explanationPane;
	}

	private void handleDetailsClick(final HyperlinkEvent evt) {
		if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			if (_parent != null) {
				final String desc = evt.getDescription();
				if ("#class".equals(desc)) {
					final BugTreePanel bugTreePanel = _parent.getBugTreePanel();
					final BugTree tree = bugTreePanel.getBugTree();
					if (bugTreePanel.isScrollToSource()) {
						tree.getScrollToSourceHandler().scollToSelectionSource();
					} else {
						bugTreePanel.setScrollToSource(true);
						tree.getScrollToSourceHandler().scollToSelectionSource();
						bugTreePanel.setScrollToSource(false);
					}
				}
			}
		}
	}

	private void editorPaneHyperlinkUpdate(final HyperlinkEvent evt) {
		try {
			if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				final URL url = evt.getURL();
				BrowserUtil.browse(url);
				_explanationPane.setPage(url);
			}
		} catch (final Exception e) {
			LOGGER.debug(e);
		}
	}

	@SuppressFBWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
	@SuppressWarnings({"HardCodedStringLiteral"})
	void setBugsDetails(@NotNull final BugInstance bugInstance) {
		final int[] lines = BugInstanceUtil.getSourceLines(bugInstance);
		final MethodAnnotation methodAnnotation = BugInstanceUtil.getPrimaryMethod(bugInstance);
		final FieldAnnotation fieldAnnotation = BugInstanceUtil.getPrimaryField(bugInstance);

		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<h2>");
		html.append(bugInstance.getAbridgedMessage());
		html.append("</h2><p/>");

		html.append("<table border=0><tr valign=top><td valign=top>");
		html.append("<h3>Class:</h3>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<a href='#class'><u>");
		html.append(BugInstanceUtil.getSimpleClassName(bugInstance));
		html.append("</u></a>");
		html.append(" <font color='gray'>(");
		final String packageName = BugInstanceUtil.getPackageName(bugInstance);
		html.append(packageName);
		html.append(")</font>");

		if (lines[0] > -1) {
			final boolean singleLine = lines[1] == lines[0];
			if (singleLine) {
				html.append(" line ");
			} else {
				html.append(" lines ");
			}
			html.append(lines[0]);
			if (!singleLine) {
				html.append('-').append(lines[1]);
			}
		}
		html.append("</ul>");

		if (methodAnnotation != null) {
			html.append("<p><h3>Method:</p>");
			html.append("<ul>");
			html.append("<li>");

			if ("<init>".equals(methodAnnotation.getMethodName())) {
				html.append(BugInstanceUtil.getJavaSourceMethodName(bugInstance)).append("&lt;init&gt; <font color='gray'>(").append(BugInstanceUtil.getFullMethod(bugInstance)).append(")</font>");
			} else {
				html.append(BugInstanceUtil.getMethodName(bugInstance)).append(" <font color='gray'>(").append(BugInstanceUtil.getFullMethod(bugInstance)).append(")</font>");
			}
			html.append("</li>");
			html.append("</ul>");
		}

		if (fieldAnnotation != null) {
			html.append("<p><h3>Field:</p>");
			html.append("<ul>");
			html.append("<li>");
			html.append(BugInstanceUtil.getFieldName(bugInstance));
			html.append("</li>");
			html.append("</ul>");
		}

		html.append("<p><h3>Priority:</p>");
		html.append("<ul>");
		html.append("<li>");
		html.append("<span width='15px' height='15px;' id='").append(BugInstanceUtil.getPriorityString(bugInstance)).append("'> &nbsp; &nbsp; </span>&nbsp;");
		html.append(BugInstanceUtil.getPriorityTypeString(bugInstance));
		html.append("</li>");
		html.append("</ul>");
		html.append("</td><td width='20px'>&nbsp;</td><td valign=top>");

		html.append("<h3>Problem classification:</h3>");
		html.append("<ul>");
		html.append("<li>");
		html.append(BugInstanceUtil.getBugCategoryDescription(bugInstance));
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getBugTypeDescription(bugInstance));
		html.append(")</font>");
		html.append("</li>");
		html.append("<li>");
		html.append(BugInstanceUtil.getBugType(bugInstance));
		html.append(" <font color='gray'>(");
		html.append(BugInstanceUtil.getBugPatternShortDescription(bugInstance));
		html.append(")</font>");
		html.append("</li>");

		final Iterable<BugAnnotation> annotations = bugInstance.getAnnotationsForMessage(false);
		if (annotations.iterator().hasNext()) {
			html.append("<p><h3>Notes:</p>");
			html.append("<ul>");
			for (final BugAnnotation annotation : annotations) {
				html.append("<li>").append(annotation.toString(bugInstance.getPrimaryClass())).append("</li>");
			}
			html.append("</ul>");
		}

		final DetectorFactory detectorFactory = bugInstance.getDetectorFactory();
		if (detectorFactory != null) {
			html.append("<li>");
			html.append(detectorFactory.getShortName());
			html.append(" <font color='gray'>(");
			html.append(createBugsAbbreviation(detectorFactory));
			html.append(")</font>");
			html.append("</li>");
		}
		html.append("</ul>");
		html.append("</tr></table>");
		html.append("</body></html>");

		// FIXME: set Suppress actions hyperlink

		_bugDetailsPane.setText(html.toString());
		scrollRectToVisible(_bugDetailsPane);

		if (_fixWithDevoxxGenieButton != null) {
			_fixWithDevoxxGenieButton.setVisible(DevoxxGenieBridge.isAvailable());
		}
	}

	void setBugExplanation(final SortedBugCollection bugCollection, final BugInstance bugInstance) {
		_lastBugCollection = bugCollection;
		_lastBugInstance = bugInstance;
		refreshDetailsShown();
	}

	private void refreshDetailsShown() {
		final String html = BugInstanceUtil.getDetailHtml(_lastBugInstance);
		// no need for BufferedReader
		try (StringReader reader = new StringReader(html)) {
			_explanationPane.setToolTipText(edu.umd.cs.findbugs.L10N
					.getLocalString("tooltip.longer_description", "This gives a longer description of the detected bug " +
																												"pattern"));
			_explanationPane.read(reader, "html bug description");
		} catch (final IOException e) {
			_explanationPane.setText("Could not find bug description: " + e.getMessage());
			LOGGER.warn(e.getMessage(), e);
		}
		scrollRectToVisible(_bugDetailsPane);
	}

	@SuppressWarnings({"AnonymousInnerClass"})
	private static void scrollRectToVisible(final JEditorPane pane) {
		SwingUtilities.invokeLater(() -> pane.scrollRectToVisible(new Rectangle(0, 0, 0, 0)));
	}

	void adaptSize(final int width, final int height) {
		//final int newWidth = (int) (width * _splitPaneHorizontalWeight);
		final int expHeight = (int) (height * 0.4);
		final int detailsHeight = (int) (height * 0.6);

		//if(_bugDetailsPanel.getPreferredSize().width != newWidth && _bugDetailsPanel.getPreferredSize().height != detailsHeight) {
		_bugDetailsPanel.setPreferredSize(new Dimension(width, detailsHeight));
		_bugDetailsPanel.setSize(new Dimension(width, detailsHeight));
		//_bugDetailsPanel.doLayout();
		_bugDetailsPanel.validate();
		//_parent.validate();
		//}

		//if(_explanationPanel.getPreferredSize().width != newWidth && _explanationPanel.getPreferredSize().height != expHeight) {
		_explanationPanel.setPreferredSize(new Dimension(width, expHeight));
		_explanationPanel.setSize(new Dimension(width, expHeight));
		//_explanationPanel.doLayout();
		_explanationPanel.validate();
		getBugDetailsSplitPane().validate();
		//_parent.validate();
		//}
	}

	public void issueUpdated(final BugInstance bug) {
		//noinspection ObjectEquality
		if (bug == _lastBugInstance) {
			refreshDetailsShown();
		}
	}

	public double getSplitPaneHorizontalWeight() {
		return _splitPaneHorizontalWeight;
	}

	public void setSplitPaneHorizontalWeight(final double splitPaneHorizontalWeight) {
		_splitPaneHorizontalWeight = splitPaneHorizontalWeight;
	}

	public void clear() {
		if (_bugDetailsPane != null) {
			_bugDetailsPane.setText(null);
		}
		if (_explanationPane != null) {
			_explanationPane.setText(null);
		}
	}

	public static String createBugsAbbreviation(final DetectorFactory factory) {
		final Collection<BugPattern> patterns = factory.getReportedBugPatterns();
		return patterns.stream().map(BugPattern::getAbbrev)
				.distinct().collect(Collectors.joining("|"));
	}

	private static class BugDetailsEditorPane extends JEditorPane {
		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}

	private static class ExplanationEditorPane extends JEditorPane {
		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}
}
