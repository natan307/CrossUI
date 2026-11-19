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
package io.github.natan307.crossui.core.renderer;

import org.bukkit.Material;
import org.geysermc.cumulus.util.FormImage;

import java.util.Locale;

/**
 * Picks the Bedrock button image for an item material.
 *
 * <p>With a URL template (config {@code icon-url-template}) the image is downloaded by the client.
 * Without one, the vanilla Bedrock texture path is used ({@code textures/blocks/..} or
 * {@code textures/items/..}), which needs no internet access but does not exist for every material.</p>
 */
public final class IconResolver {

    /** A resolved image: its type and its value. */
    public record Icon(FormImage.Type type, String value) { }

    private IconResolver() {
    }

    public static Icon resolve(String materialKey, String urlTemplate) {
        String key = materialKey.toLowerCase(Locale.ROOT);
        if (urlTemplate != null && !urlTemplate.isBlank()) {
            return new Icon(FormImage.Type.URL, urlTemplate.replace("%material%", key));
        }

        Material material = Material.matchMaterial(key);
        boolean block = material != null && material.isBlock();
        String folder = block ? "textures/blocks/" : "textures/items/";
        return new Icon(FormImage.Type.PATH, folder + bedrockName(key, block));
    }

    /** Maps the most common Java names that differ from the Bedrock texture names. */
    private static String bedrockName(String key, boolean block) {
        if (!block) {
            return key;
        }
        if (key.equals("grass_block")) {
            return "grass_side_carried";
        }
        if (key.endsWith("_planks")) {
            return "planks_" + key.substring(0, key.length() - "_planks".length());
        }
        if (key.endsWith("_log")) {
            return "log_" + key.substring(0, key.length() - "_log".length());
        }
        if (key.endsWith("_wool")) {
            return "wool_colored_" + key.substring(0, key.length() - "_wool".length());
        }
        return key;
    }
}
