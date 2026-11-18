/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.api.model;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Universal button representation. Text supports legacy {@code &} color codes.
 *
 * <p>The optional icon is interpreted per platform: {@code "url:https://..."} and
 * {@code "path:textures/..."} for Bedrock forms, or a Bukkit material name for
 * the classic inventory fallback.</p>
 */
public final class UIButton {

    private final String title;
    private final String subtext;
    private final String icon;
    private final Consumer<Player> action;

    public UIButton(String title, String subtext, String icon, Consumer<Player> action) {
        this.title = Objects.requireNonNull(title, "title");
        this.subtext = subtext;
        this.icon = icon;
        this.action = action == null ? p -> { } : action;
    }

    public static UIButton of(String title, Consumer<Player> action) {
        return new UIButton(title, null, null, action);
    }

    public String title() {
        return title;
    }

    /** Secondary line, may be {@code null}. */
    public String subtext() {
        return subtext;
    }

    /** Icon descriptor, may be {@code null}. */
    public String icon() {
        return icon;
    }

    public Consumer<Player> action() {
        return action;
    }
}
