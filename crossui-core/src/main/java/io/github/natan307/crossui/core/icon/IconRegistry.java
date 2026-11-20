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

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Translates Java item ids into the vanilla Bedrock texture paths shown as button images.
 *
 * <p>The table ({@code bedrock-icons.txt}) only contains texture <em>names</em>. The pictures
 * themselves come from the player's own Bedrock client, so no image is ever sent or bundled.
 * Resolution order: explicit config override, URL template, built-in table.</p>
 */
public final class IconRegistry {

    private static final String RESOURCE = "/bedrock-icons.txt";

    private final Map<String, String> builtIn;
    private final java.util.Set<String> shaped;

    private IconRegistry(Map<String, String> builtIn, java.util.Set<String> shaped) {
        this.builtIn = builtIn;
        this.shaped = shaped;
    }

    /** Loads the built-in table from the plugin jar. Never throws: a broken table just means no icons. */
    public static IconRegistry load(JavaPlugin plugin) {
        try (InputStream in = IconRegistry.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                plugin.getLogger().warning("bedrock-icons.txt is missing from the jar, button icons are disabled.");
                return new IconRegistry(Map.of(), java.util.Set.of());
            }
            return new IconRegistry(parse(in), loadShaped());
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read bedrock-icons.txt, button icons are disabled.", e);
            return new IconRegistry(Map.of(), java.util.Set.of());
        }
    }

    /** Ids that have a rendered picture in the icon pack (slabs, stairs, fences...). Empty when the list is missing. */
    private static java.util.Set<String> loadShaped() {
        try (InputStream in = IconRegistry.class.getResourceAsStream("/bedrock-shaped.txt")) {
            if (in == null) {
                return java.util.Set.of();
            }
            java.util.Set<String> set = new java.util.HashSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && line.charAt(0) != '#') {
                        set.add(line);
                    }
                }
            }
            return java.util.Set.copyOf(set);
        } catch (IOException | RuntimeException e) {
            return java.util.Set.of();
        }
    }

    /** @return true when the icon pack has a rendered picture for this item id */
    public boolean hasShaped(String materialKey) {
        return shaped.contains(materialKey.toLowerCase(Locale.ROOT));
    }

    /** Parses {@code java_id=items/name} lines; blank lines and lines starting with {@code #} are ignored. */
    public static Map<String, String> parse(InputStream in) throws IOException {
        Map<String, String> map = new HashMap<>(2048);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq > 0 && eq < line.length() - 1) {
                    map.put(line.substring(0, eq), line.substring(eq + 1));
                }
            }
        }
        return Map.copyOf(map);
    }

    public int size() {
        return builtIn.size();
    }

    /**
     * @param materialKey Java item id such as {@code diamond_sword}
     * @return the Bedrock texture path, for example {@code textures/items/diamond_sword}, or {@code null}
     */
    public String texturePath(String materialKey) {
        String value = builtIn.get(materialKey.toLowerCase(Locale.ROOT));
        return value == null ? null : "textures/" + value;
    }
}
