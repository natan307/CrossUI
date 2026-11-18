/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.parser;

import java.util.List;

/**
 * One clickable entry extracted from a chest inventory.
 *
 * @param slot        original container slot, used when injecting the click
 * @param name        display name
 * @param subtext     first meaningful lore line, or {@code null}
 * @param lore        all lore lines
 * @param materialKey item material key such as {@code diamond_sword}
 * @param leftAction  lore line describing the left-click action, or {@code null}
 * @param rightAction lore line describing the right-click action, or {@code null}
 */
public record ParsedButton(
        int slot,
        String name,
        String subtext,
        List<String> lore,
        String materialKey,
        String leftAction,
        String rightAction
) {

    /** True when the lore advertises two different actions (left and right click). */
    public boolean dual() {
        return leftAction != null && rightAction != null;
    }
}
