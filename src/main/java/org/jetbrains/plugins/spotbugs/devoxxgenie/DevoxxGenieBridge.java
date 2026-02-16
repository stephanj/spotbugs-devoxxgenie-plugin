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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class DevoxxGenieBridge {

	private static final Logger LOG = Logger.getInstance(DevoxxGenieBridge.class);

	private static final String DEVOXX_GENIE_PLUGIN_ID = "com.devoxx.genie";
	private static final String EXTERNAL_PROMPT_SERVICE_CLASS = "com.devoxx.genie.service.ExternalPromptService";

	private DevoxxGenieBridge() {
	}

	private static ClassLoader getPluginClassLoader() {
		IdeaPluginDescriptor descriptor = PluginManagerCore.getPlugin(PluginId.getId(DEVOXX_GENIE_PLUGIN_ID));
		if (descriptor == null || !descriptor.isEnabled()) {
			return null;
		}
		return descriptor.getPluginClassLoader();
	}

	public static boolean isAvailable() {
		return getPluginClassLoader() != null;
	}

	public static boolean sendPrompt(@NotNull Project project, @NotNull String text) {
		try {
			ClassLoader classLoader = getPluginClassLoader();
			if (classLoader == null) {
				return false;
			}
			Class<?> serviceClass = Class.forName(EXTERNAL_PROMPT_SERVICE_CLASS, true, classLoader);
			Object service = serviceClass.getMethod("getInstance", Project.class).invoke(null, project);
			if (service == null) {
				LOG.warn("DevoxxGenie ExternalPromptService.getInstance() returned null");
				return false;
			}
			Object result = serviceClass.getMethod("setPromptText", String.class).invoke(service, text);
			boolean success = result instanceof Boolean && (Boolean) result;
			if (!success) {
				LOG.warn("DevoxxGenie setPromptText returned false — tool window may not have been opened yet");
			}
			return success;
		} catch (Exception e) {
			LOG.warn("Failed to send prompt to DevoxxGenie", e);
			return false;
		}
	}
}
