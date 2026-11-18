/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.api;

import io.github.natan307.crossui.api.builder.ConfirmBuilder;
import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.api.builder.MenuBuilder;
import io.github.natan307.crossui.api.platform.ClientPlatform;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Main service contract of CrossUI.
 *
 * <p>The CrossUI plugin registers an implementation in the Bukkit
 * {@link org.bukkit.plugin.ServicesManager}, so it can be obtained the same way as Vault's
 * {@code Economy}. Most plugins should simply use the static {@link CrossUI} facade.</p>
 *
 * <p>Only plain Java and Bukkit types appear in this API, never PacketEvents, Geyser or
 * Floodgate types.</p>
 */
public interface CrossUIProvider {

    /** Detects which UI family the given player's client supports. */
    @NotNull ClientPlatform getPlatform(@NotNull Player player);

    /** Starts building a button menu. */
    @NotNull MenuBuilder createMenu(@NotNull Player player);

    /** Starts building a single-field text input. */
    @NotNull InputBuilder createInput(@NotNull Player player);

    /** Starts building a Yes/No confirmation dialog. */
    @NotNull ConfirmBuilder createConfirm(@NotNull Player player);
}
