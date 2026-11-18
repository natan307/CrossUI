/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the crossui-api directory.
 */
package io.github.natan307.crossui.api;

import io.github.natan307.crossui.api.builder.ConfirmBuilder;
import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.api.builder.MenuBuilder;
import io.github.natan307.crossui.api.platform.ClientPlatform;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Static facade of the CrossUI API.
 *
 * <p>One declarative builder call is rendered as a Bedrock Form, a Java Native Dialog or a classic
 * inventory GUI, depending on the client of the target player.</p>
 *
 * <pre>{@code
 * CrossUI.input(player)
 *     .title("Transfer Balance")
 *     .label("Enter the amount to send:")
 *     .placeholder("Example: 5000")
 *     .onSubmit((p, text) -> bank.transfer(p, text))
 *     .open();
 * }</pre>
 *
 * <p><b>Soft-depend tip:</b> never reference this class from a class that is loaded when CrossUI
 * is missing. Isolate it behind your own interface, see the Developer API wiki page.</p>
 */
public final class CrossUI {

    /** Version of the public API contract. */
    public static final String API_VERSION = "1.0";

    private static volatile CrossUIProvider provider;

    private CrossUI() {
    }

    /** Used by the CrossUI plugin. Third-party plugins must not call this. */
    public static void setProvider(@NotNull CrossUIProvider instance) {
        provider = instance;
    }

    /** Used by the CrossUI plugin. Third-party plugins must not call this. */
    public static void clearProvider() {
        provider = null;
    }

    /** Returns {@code true} when the CrossUI plugin is installed and enabled. */
    public static boolean isAvailable() {
        try {
            get();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Returns the active provider. Falls back to the Bukkit ServicesManager, which is safe across
     * reloads and multiple class loaders.
     *
     * @throws IllegalStateException when CrossUI is not installed or not enabled yet
     */
    @NotNull
    public static CrossUIProvider get() {
        CrossUIProvider current = provider;
        if (current != null) {
            return current;
        }

        RegisteredServiceProvider<CrossUIProvider> registration =
                Bukkit.getServicesManager().getRegistration(CrossUIProvider.class);
        if (registration != null) {
            provider = registration.getProvider();
            return provider;
        }
        throw new IllegalStateException(
                "CrossUI is not installed or not enabled yet. Add CrossUI to your plugin.yml depend/softdepend list.");
    }

    // ----------------------------------------------------------- shortcuts

    /** Detects which UI family the given player's client supports. */
    public static @NotNull ClientPlatform platform(@NotNull Player player) {
        return get().getPlatform(player);
    }

    /** Starts building a button menu. */
    public static @NotNull MenuBuilder menu(@NotNull Player player) {
        return get().createMenu(player);
    }

    /** Starts building a single-field text input. */
    public static @NotNull InputBuilder input(@NotNull Player player) {
        return get().createInput(player);
    }

    /** Starts building a Yes/No confirmation dialog. */
    public static @NotNull ConfirmBuilder confirm(@NotNull Player player) {
        return get().createConfirm(player);
    }
}
