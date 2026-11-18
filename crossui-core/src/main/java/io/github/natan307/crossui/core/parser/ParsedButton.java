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
