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

import com.intellij.openapi.components.Service;
import edu.umd.cs.findbugs.BugInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Project-level service that tracks which {@link BugInstance} objects have been
 * checked by the user for bulk DevoxxGenie task creation.
 *
 * <p>Uses identity equality so that two structurally identical {@code BugInstance}
 * objects are treated as different entries.</p>
 */
@Service(Service.Level.PROJECT)
public final class BugSelectionManager {

    private final Set<BugInstance> selectedBugs = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Runnable> listeners = new ArrayList<>();

    public void toggle(@NotNull BugInstance bug) {
        if (selectedBugs.contains(bug)) {
            selectedBugs.remove(bug);
        } else {
            selectedBugs.add(bug);
        }
        fireListeners();
    }

    public boolean isSelected(@NotNull BugInstance bug) {
        return selectedBugs.contains(bug);
    }

    public int getSelectedCount() {
        return selectedBugs.size();
    }

    @NotNull
    public Set<BugInstance> getSelectedBugs() {
        return Collections.unmodifiableSet(new HashSet<>(selectedBugs));
    }

    public void clear() {
        selectedBugs.clear();
        fireListeners();
    }

    public void addListener(@NotNull Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(@NotNull Runnable listener) {
        listeners.remove(listener);
    }

    private void fireListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
