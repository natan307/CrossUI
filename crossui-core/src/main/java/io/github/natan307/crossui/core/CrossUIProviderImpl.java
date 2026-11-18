/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.natan307.crossui.api.CrossUIProvider;
import io.github.natan307.crossui.api.builder.ConfirmBuilder;
import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.api.builder.MenuBuilder;
import io.github.natan307.crossui.api.platform.ClientPlatform;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.geysermc.cumulus.form.Form;

/** Implementation of the public API: picks the best renderer for the player's client. */
final class CrossUIProviderImpl implements CrossUIProvider {

    private final CrossUIPlugin plugin;

    CrossUIProviderImpl(CrossUIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull ClientPlatform getPlatform(@NotNull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        ClientVersion version = user == null ? ClientVersion.UNKNOWN : user.getClientVersion();
        return plugin.platforms().resolve(player.getUniqueId(), version);
    }

    @Override
    public @NotNull MenuBuilder createMenu(@NotNull Player player) {
        return new MenuBuilder(player, this::open);
    }

    @Override
    public @NotNull InputBuilder createInput(@NotNull Player player) {
        return new InputBuilder(player, this::open);
    }

    @Override
    public @NotNull ConfirmBuilder createConfirm(@NotNull Player player) {
        return new ConfirmBuilder(player, this::open);
    }

    private void open(MenuBuilder menu) {
        Player player = menu.player();
        switch (getPlatform(player)) {
            case BEDROCK -> {
                if (!sendForm(player, plugin.bedrockRenderer().menuForm(menu))) {
                    plugin.legacyRenderer().menu(player, menu);
                }
            }
            case JAVA_DIALOG -> {
                if (!plugin.dialogs().menu(player, menu)) {
                    plugin.legacyRenderer().menu(player, menu);
                }
            }
            case JAVA_LEGACY -> plugin.legacyRenderer().menu(player, menu);
        }
    }

    private void open(InputBuilder input) {
        Player player = input.player();
        switch (getPlatform(player)) {
            case BEDROCK -> {
                if (!sendForm(player, plugin.bedrockRenderer().inputForm(input))) {
                    plugin.legacyRenderer().input(player, input);
                }
            }
            case JAVA_DIALOG -> {
                if (!plugin.dialogs().input(player, input)) {
                    plugin.legacyRenderer().input(player, input);
                }
            }
            case JAVA_LEGACY -> plugin.legacyRenderer().input(player, input);
        }
    }

    private void open(ConfirmBuilder confirm) {
        Player player = confirm.player();
        switch (getPlatform(player)) {
            case BEDROCK -> {
                if (!sendForm(player, plugin.bedrockRenderer().confirmForm(confirm))) {
                    plugin.legacyRenderer().confirm(player, confirm);
                }
            }
            case JAVA_DIALOG -> {
                if (!plugin.dialogs().confirm(player, confirm)) {
                    plugin.legacyRenderer().confirm(player, confirm);
                }
            }
            case JAVA_LEGACY -> plugin.legacyRenderer().confirm(player, confirm);
        }
    }

    private boolean sendForm(Player player, Form form) {
        return plugin.bridge() != null && plugin.bridge().sendForm(player.getUniqueId(), form);
    }
}
