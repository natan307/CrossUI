/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.renderer;

import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.core.CrossUIPlugin;
import io.github.natan307.crossui.core.parser.TextSanitizer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Text input for legacy Java clients: the next chat message of the player is the answer. */
public final class ChatInputService implements Listener {

    private static final String CANCEL_WORD = "cancel";

    private final CrossUIPlugin plugin;
    private final Map<UUID, InputBuilder> waiting = new ConcurrentHashMap<>();

    public ChatInputService(CrossUIPlugin plugin) {
        this.plugin = plugin;
    }

    public void request(Player player, InputBuilder input) {
        waiting.put(player.getUniqueId(), input);
        String prefix = plugin.config().rawMessage("prefix");
        player.sendMessage(prefix + TextSanitizer.toBedrock(input.title()));
        if (!input.label().isEmpty()) {
            player.sendMessage(prefix + TextSanitizer.toBedrock(input.label()));
        }
        player.sendMessage(prefix + plugin.config().rawMessage("chat-input-hint"));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        InputBuilder input = waiting.remove(event.getPlayer().getUniqueId());
        if (input == null) {
            return;
        }
        event.setCancelled(true);
        String text = event.getMessage();
        Player player = event.getPlayer();

        if (text.equalsIgnoreCase(CANCEL_WORD)) {
            if (input.onCancel() != null) {
                plugin.scheduler().runForPlayer(player, () -> input.onCancel().accept(player));
            }
            return;
        }
        plugin.scheduler().runForPlayer(player, () -> input.onSubmit().accept(player, text));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        waiting.remove(event.getPlayer().getUniqueId());
    }

    public void clear() {
        waiting.clear();
    }
}
