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
package io.github.natan307.crossui.core.platform;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.github.natan307.crossui.api.platform.ClientPlatform;
import io.github.natan307.crossui.core.config.BridgeConfig;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Works out which UI family each player's client can show. Results are cached per player and
 * every lookup is cheap and thread-safe, so it may be called from Netty threads.
 */
public final class PlatformResolver {

    /** Protocol number of Minecraft 1.21.6, the first version with native dialogs. */
    private static final int DIALOG_PROTOCOL = 771;

    private final Map<UUID, ClientPlatform> cache = new ConcurrentHashMap<>();
    private final Supplier<BedrockBridge> bridge;
    private final BridgeConfig config;
    private final boolean serverSupportsDialogs;

    public PlatformResolver(Supplier<BedrockBridge> bridge, BridgeConfig config) {
        this.bridge = bridge;
        this.config = config;
        this.serverSupportsDialogs = PacketEvents.getAPI().getServerManager().getVersion()
                .isNewerThanOrEquals(ServerVersion.V_1_21_6);
    }

    /** Whether this server version can send dialog packets at all. */
    public boolean serverSupportsDialogs() {
        return serverSupportsDialogs;
    }

    public ClientPlatform resolve(UUID uuid, ClientVersion version) {
        ClientPlatform cached = cache.get(uuid);
        if (cached != null) {
            return cached;
        }

        BedrockBridge b = bridge.get();
        if (b != null && b.isBedrock(uuid)) {
            cache.put(uuid, ClientPlatform.BEDROCK);
            return ClientPlatform.BEDROCK;
        }

        if (version == null || version == ClientVersion.UNKNOWN) {
            // Protocol not known yet: answer conservatively without caching.
            return ClientPlatform.JAVA_LEGACY;
        }

        boolean dialogs = serverSupportsDialogs
                && config.nativeDialogs()
                && version.getProtocolVersion() >= DIALOG_PROTOCOL;
        ClientPlatform platform = dialogs ? ClientPlatform.JAVA_DIALOG : ClientPlatform.JAVA_LEGACY;
        cache.put(uuid, platform);
        return platform;
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }

    /** Number of cached players per platform, for metrics. */
    public Map<ClientPlatform, Integer> counts() {
        Map<ClientPlatform, Integer> out = new EnumMap<>(ClientPlatform.class);
        for (ClientPlatform p : cache.values()) {
            out.merge(p, 1, Integer::sum);
        }
        return out;
    }
}
