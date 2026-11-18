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

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.github.natan307.crossui.core.config.BridgeConfig;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Heuristics that turn the item list of a chest inventory into a list of real buttons:
 * decorative frame items are dropped and click hints in the lore are detected.
 */
public final class InventoryFilter {

    private final BridgeConfig config;
    private final TextSanitizer text;

    public InventoryFilter(BridgeConfig config, TextSanitizer text) {
        this.config = config;
        this.text = text;
    }

    /**
     * @param items         all window items as sent by the server
     * @param containerSize number of container slots (rows * 9); player inventory slots are ignored
     */
    public List<ParsedButton> parse(List<ItemStack> items, int containerSize) {
        List<ParsedButton> out = new ArrayList<>();
        int limit = Math.min(containerSize, items.size());

        for (int slot = 0; slot < limit; slot++) {
            ItemStack item = items.get(slot);
            if (item == null || item.isEmpty()) {
                continue;
            }

            String material = item.getType().getName().getKey();

            Optional<Component> customName = item.getComponent(ComponentTypes.CUSTOM_NAME);
            String plainName = customName.map(text::plain).orElse("");
            boolean blank = text.isBlank(plainName);

            if (blank && (isDecorationMaterial(material) || !config.includeUnnamed())) {
                continue;
            }

            String name;
            if (!blank) {
                name = config.keepColors() ? customName.map(text::colored).orElse(plainName) : plainName;
            } else {
                name = item.getComponent(ComponentTypes.ITEM_NAME).map(text::plain)
                        .filter(s -> !text.isBlank(s))
                        .orElse(prettify(material));
            }

            List<String> lore = new ArrayList<>();
            Optional<ItemLore> loreComponent = item.getComponent(ComponentTypes.LORE);
            if (loreComponent.isPresent()) {
                for (Component line : loreComponent.get().getLines()) {
                    String l = text.plain(line);
                    if (!l.isEmpty()) {
                        lore.add(l);
                    }
                }
            }

            String left = null;
            String right = null;
            String subtext = null;
            for (String line : lore) {
                String lower = line.toLowerCase(Locale.ROOT);
                boolean isLeft = containsAny(lower, config.leftKeywords());
                boolean isRight = containsAny(lower, config.rightKeywords());
                if (isLeft && left == null) {
                    left = line;
                }
                if (isRight && right == null) {
                    right = line;
                }
                if (!isLeft && !isRight && subtext == null && !text.isBlank(line)) {
                    subtext = line;
                }
            }

            out.add(new ParsedButton(slot, name, subtext, List.copyOf(lore), material, left, right));
        }
        return out;
    }

    private boolean isDecorationMaterial(String material) {
        for (String pattern : config.decorationMaterials()) {
            if (pattern.indexOf('*') < 0) {
                if (pattern.equals(material)) {
                    return true;
                }
            } else if (material.matches(pattern.replace("*", ".*"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String haystack, List<String> needles) {
        for (String needle : needles) {
            if (!needle.isEmpty() && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String prettify(String materialKey) {
        StringBuilder sb = new StringBuilder();
        for (String part : materialKey.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
