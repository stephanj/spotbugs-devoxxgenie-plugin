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
package org.jetbrains.plugins.spotbugs.common.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.ThrowableComputable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helpers for running model-touching UI code on the EDT under the correct lock.
 * <p>
 * Modern IntelliJ platforms (2024.1+) no longer hold an implicit write-intent read lock during EDT
 * events, so code that reads the PSI/document model and/or runs a write command must request the
 * lock explicitly. {@code Application.runWriteIntentReadAction} does not exist in the declared
 * minimum platform (2023.3), so it is invoked reflectively with a graceful fallback for older IDEs,
 * where the EDT still holds the lock implicitly.
 */
public final class ThreadingUtilFb {

	/** {@code Application.runWriteIntentReadAction(ThrowableComputable)} on platforms that have it (2024.1+), else null. Resolved once. */
	@Nullable
	private static final Method WRITE_INTENT_READ_ACTION_METHOD = resolveWriteIntentReadActionMethod();

	private ThreadingUtilFb() {
	}

	/**
	 * Runs {@code task} inside a write-intent read action (read access + permission to mutate the
	 * editor/document/PSI model or run a write command).
	 * <p>
	 * If the caller is already inside a read action (e.g. an action toolbar update that runs under
	 * {@code runReadAction}), a write-intent read action cannot be acquired — a read lock cannot be
	 * upgraded. In that case the work is deferred to a later EDT cycle where no read lock is held.
	 */
	public static void runWriteIntentReadAction(@NotNull final Runnable task) {
		final Application application = ApplicationManager.getApplication();
		final Method method = WRITE_INTENT_READ_ACTION_METHOD;
		if (method == null) {
			// Older platform: the EDT already runs under an implicit write-intent read action.
			task.run();
			return;
		}
		if (application.isReadAccessAllowed()) {
			// Inside a read action: defer so the write-intent action is taken outside the read lock.
			application.invokeLater(() -> invokeWriteIntentReadAction(method, application, task));
			return;
		}
		invokeWriteIntentReadAction(method, application, task);
	}

	private static void invokeWriteIntentReadAction(@NotNull final Method method, @NotNull final Application application, @NotNull final Runnable task) {
		final ThrowableComputable<Object, RuntimeException> computable = () -> {
			task.run();
			return null;
		};
		try {
			method.invoke(application, computable);
		} catch (final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new RuntimeException(cause);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	private static Method resolveWriteIntentReadActionMethod() {
		try {
			return ApplicationManager.getApplication().getClass().getMethod("runWriteIntentReadAction", ThrowableComputable.class);
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}
}
