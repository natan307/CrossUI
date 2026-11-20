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
package io.github.natan307.crossui.core.icon;

import io.github.natan307.crossui.core.config.BridgeConfig;
import org.geysermc.cumulus.util.FormImage;

import java.util.Locale;

/** Chooses the button image of an item: config override, then URL template, then the built-in table. */
public final class IconResolver {

    private final IconRegistry registry;
    private final BridgeConfig config;

    public IconResolver(IconRegistry registry, BridgeConfig config) {
        this.registry = registry;
        this.config = config;
    }

    /** @return the image for a player head with the given skin, falling back to the normal item picture */
    public BedrockIcon resolve(String materialKey, String headValue) {
        String headTemplate = config.headUrlTemplate();
        if (headValue != null && headTemplate != null && !headTemplate.isBlank()) {
            return new BedrockIcon(FormImage.Type.URL, headTemplate.replace("%value%", headValue));
        }
        return resolve(materialKey);
    }

    /** @return the image for the item, or {@code null} when the button should have no image */
    public BedrockIcon resolve(String materialKey) {
        if (materialKey == null || materialKey.isEmpty()) {
            return null;
        }
        String key = materialKey.toLowerCase(Locale.ROOT);

        String override = config.iconOverrides().get(key);
        if (override != null) {
            return fromValue(override);
        }

        String template = config.iconUrlTemplate();
        if (template != null && !template.isBlank()) {
            return new BedrockIcon(FormImage.Type.URL, template.replace("%material%", key));
        }

        String pack = config.iconPackUrl();
        if (pack != null && !pack.isBlank() && registry.hasShaped(key)) {
            return new BedrockIcon(FormImage.Type.URL, (pack.endsWith("/") ? pack : pack + "/") + key + ".png");
        }

        for (String pattern : config.iconExclude()) {
            if (pattern.startsWith("*") ? key.endsWith(pattern.substring(1)) : key.equals(pattern)) {
                return null;
            }
        }

        String path = registry.texturePath(key);
        return path == null ? null : new BedrockIcon(FormImage.Type.PATH, path);
    }

    private static BedrockIcon fromValue(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return new BedrockIcon(FormImage.Type.URL, value);
        }
        return new BedrockIcon(FormImage.Type.PATH, value);
    }
}
