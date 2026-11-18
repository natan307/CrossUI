/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the crossui-api directory.
 */
package io.github.natan307.crossui.api.builder;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Unified builder for a single-field text input. */
public final class InputBuilder {

    private final Player player;
    private final Consumer<InputBuilder> opener;
    private String title = "Input";
    private String label = "";
    private String placeholder = "";
    private String defaultValue = "";
    private BiConsumer<Player, String> onSubmit = (p, text) -> { };
    private Consumer<Player> onCancel;

    /**
     * Internal: builders are created through {@link io.github.natan307.crossui.api.CrossUI}.
     * The opener is supplied by the CrossUI plugin.
     */
    public InputBuilder(Player player, Consumer<InputBuilder> opener) {
        this.player = Objects.requireNonNull(player, "player");
        this.opener = Objects.requireNonNull(opener, "opener");
    }

    public InputBuilder title(String title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    public InputBuilder label(String label) {
        this.label = Objects.requireNonNull(label, "label");
        return this;
    }

    public InputBuilder placeholder(String placeholder) {
        this.placeholder = Objects.requireNonNull(placeholder, "placeholder");
        return this;
    }

    public InputBuilder defaultValue(String defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        return this;
    }

    /** Called on the main server thread with the raw text the player typed. */
    public InputBuilder onSubmit(BiConsumer<Player, String> onSubmit) {
        this.onSubmit = Objects.requireNonNull(onSubmit, "onSubmit");
        return this;
    }

    /** Called when the player cancels or closes the input (not supported by Java Dialogs). */
    public InputBuilder onCancel(Consumer<Player> onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    public Player player() {
        return player;
    }

    public String title() {
        return title;
    }

    public String label() {
        return label;
    }

    public String placeholder() {
        return placeholder;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public BiConsumer<Player, String> onSubmit() {
        return onSubmit;
    }

    public Consumer<Player> onCancel() {
        return onCancel;
    }

    /** Shows the input using the best UI the player's client supports. */
    public void open() {
        opener.accept(this);
    }
}
