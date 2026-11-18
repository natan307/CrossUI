/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.api.builder;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Consumer;

/** Unified builder for a Yes/No confirmation dialog. */
public final class ConfirmBuilder {

    private final Player player;
    private final Consumer<ConfirmBuilder> opener;
    private String title = "Confirm";
    private String body = "";
    private String yesText = "Yes";
    private String noText = "No";
    private Consumer<Player> onYes = p -> { };
    private Consumer<Player> onNo = p -> { };

    /**
     * Internal: builders are created through {@link io.github.natan307.crossui.api.CrossUI}.
     * The opener is supplied by the CrossUI plugin.
     */
    public ConfirmBuilder(Player player, Consumer<ConfirmBuilder> opener) {
        this.player = Objects.requireNonNull(player, "player");
        this.opener = Objects.requireNonNull(opener, "opener");
    }

    public ConfirmBuilder title(String title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    public ConfirmBuilder body(String body) {
        this.body = Objects.requireNonNull(body, "body");
        return this;
    }

    public ConfirmBuilder yesText(String yesText) {
        this.yesText = Objects.requireNonNull(yesText, "yesText");
        return this;
    }

    public ConfirmBuilder noText(String noText) {
        this.noText = Objects.requireNonNull(noText, "noText");
        return this;
    }

    public ConfirmBuilder onYes(Consumer<Player> onYes) {
        this.onYes = Objects.requireNonNull(onYes, "onYes");
        return this;
    }

    /** Called on "No" and, where the platform supports it, when the dialog is dismissed. */
    public ConfirmBuilder onNo(Consumer<Player> onNo) {
        this.onNo = Objects.requireNonNull(onNo, "onNo");
        return this;
    }

    public Player player() {
        return player;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public String yesText() {
        return yesText;
    }

    public String noText() {
        return noText;
    }

    public Consumer<Player> onYes() {
        return onYes;
    }

    public Consumer<Player> onNo() {
        return onNo;
    }

    /** Shows the dialog using the best UI the player's client supports. */
    public void open() {
        opener.accept(this);
    }
}
