/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
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
