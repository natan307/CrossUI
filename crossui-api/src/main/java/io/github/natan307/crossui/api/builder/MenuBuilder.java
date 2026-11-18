/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.api.builder;

import io.github.natan307.crossui.api.model.UIButton;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Unified builder for a menu made of buttons. */
public final class MenuBuilder {

    private final Player player;
    private final Consumer<MenuBuilder> opener;
    private final List<UIButton> buttons = new ArrayList<>();
    private String title = "Menu";
    private String content = "";
    private Consumer<Player> onClose;

    /**
     * Internal: builders are created through {@link io.github.natan307.crossui.api.CrossUI}.
     * The opener is supplied by the CrossUI plugin.
     */
    public MenuBuilder(Player player, Consumer<MenuBuilder> opener) {
        this.player = Objects.requireNonNull(player, "player");
        this.opener = Objects.requireNonNull(opener, "opener");
    }

    public MenuBuilder title(String title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    public MenuBuilder content(String content) {
        this.content = Objects.requireNonNull(content, "content");
        return this;
    }

    public MenuBuilder button(UIButton button) {
        this.buttons.add(Objects.requireNonNull(button, "button"));
        return this;
    }

    public MenuBuilder button(String title, Consumer<Player> action) {
        return button(new UIButton(title, null, null, action));
    }

    public MenuBuilder button(String title, String subtext, Consumer<Player> action) {
        return button(new UIButton(title, subtext, null, action));
    }

    public MenuBuilder button(String title, String subtext, String icon, Consumer<Player> action) {
        return button(new UIButton(title, subtext, icon, action));
    }

    /** Called when the player dismisses the menu without choosing a button (not supported by Java Dialogs). */
    public MenuBuilder onClose(Consumer<Player> onClose) {
        this.onClose = onClose;
        return this;
    }

    public Player player() {
        return player;
    }

    public String title() {
        return title;
    }

    public String content() {
        return content;
    }

    public List<UIButton> buttons() {
        return Collections.unmodifiableList(buttons);
    }

    public Consumer<Player> onClose() {
        return onClose;
    }

    /** Shows the menu using the best UI the player's client supports. */
    public void open() {
        opener.accept(this);
    }
}
