/*
 * CrossUI - Cross-platform UI bridge between Java Chest GUIs, Native Dialogs, and Bedrock Forms.
 * Copyright (C) 2026  natan307
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.natan307.crossui.core.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Per-player session store. Entries are removed on quit so nothing leaks. */
public final class SessionManager {

    private final Map<UUID, UISession> sessions = new ConcurrentHashMap<>();

    public UISession get(UUID uuid) {
        return sessions.get(uuid);
    }

    public void put(UUID uuid, UISession session) {
        sessions.put(uuid, session);
    }

    /** Removes the session of the player regardless of which one it is. */
    public void remove(UUID uuid) {
        sessions.remove(uuid);
    }

    /** Removes the session only if it is still the given instance. */
    public void remove(UUID uuid, UISession expected) {
        sessions.remove(uuid, expected);
    }

    public void clear() {
        sessions.clear();
    }

    public int size() {
        return sessions.size();
    }
}
