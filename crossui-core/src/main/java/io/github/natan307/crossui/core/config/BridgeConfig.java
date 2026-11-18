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
package io.github.natan307.crossui.core.config;

import io.github.natan307.crossui.core.CrossUIPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Loads and exposes config.yml. All reads are thread-safe (values are swapped on reload). */
public final class BridgeConfig {

    private final CrossUIPlugin plugin;

    private volatile FileConfiguration config;
    private volatile boolean debug;
    private volatile boolean convertChestGuis;
    private volatile boolean whitelistMode;
    private volatile boolean includeUnnamed;
    private volatile boolean showLoreInButton;
    private volatile boolean icons;
    private volatile boolean keepColors;
    private volatile boolean nativeDialogs;
    private volatile boolean fallbackWhenEmpty;
    private volatile int loreMaxLength;
    private volatile int refreshDelayTicks;
    private volatile String formContent;
    private volatile List<String> titleBlacklist;
    private volatile List<String> titleWhitelist;
    private volatile List<String> decorationMaterials;
    private volatile List<String> leftKeywords;
    private volatile List<String> rightKeywords;

    public BridgeConfig(CrossUIPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();
        this.config = c;

        this.debug = c.getBoolean("debug", false);

        ConfigurationSection b = c.getConfigurationSection("bedrock");
        if (b == null) {
            b = c.createSection("bedrock");
        }
        this.convertChestGuis = b.getBoolean("convert-chest-guis", true);
        this.titleBlacklist = b.getStringList("title-blacklist");
        this.whitelistMode = b.getBoolean("whitelist-mode", false);
        this.titleWhitelist = b.getStringList("title-whitelist");
        this.decorationMaterials = lower(b.getStringList("decoration-materials"));
        this.includeUnnamed = b.getBoolean("include-unnamed-items", false);
        this.showLoreInButton = b.getBoolean("show-lore-in-button", true);
        this.loreMaxLength = Math.max(8, b.getInt("lore-max-length", 40));
        this.keepColors = b.getBoolean("keep-colors", false);
        this.icons = b.getBoolean("icons", false);
        this.formContent = b.getString("form-content", "");
        this.leftKeywords = lower(b.getStringList("dual-click.left-keywords"));
        this.rightKeywords = lower(b.getStringList("dual-click.right-keywords"));
        this.refreshDelayTicks = Math.max(1, b.getInt("refresh-delay-ticks", 4));
        this.fallbackWhenEmpty = b.getBoolean("fallback-to-chest-when-empty", true);

        this.nativeDialogs = c.getBoolean("java.native-dialogs", true);
    }

    private static List<String> lower(List<String> in) {
        return in.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
    }

    /** Applies blacklist / whitelist rules to a plain-text menu title. */
    public boolean isTitleAllowed(String plainTitle) {
        if (matchesAny(titleBlacklist, plainTitle)) {
            return false;
        }
        return !whitelistMode || matchesAny(titleWhitelist, plainTitle);
    }

    private static boolean matchesAny(List<String> entries, String title) {
        String lowerTitle = title.toLowerCase(Locale.ROOT);
        for (String entry : entries) {
            if (entry.regionMatches(true, 0, "regex:", 0, 6)) {
                try {
                    if (Pattern.compile(entry.substring(6), Pattern.CASE_INSENSITIVE).matcher(title).find()) {
                        return true;
                    }
                } catch (PatternSyntaxException ignored) {
                    // invalid pattern: ignore the entry
                }
            } else if (!entry.isBlank() && lowerTitle.contains(entry.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /** Returns a configured message with the prefix applied and color codes translated. */
    public String message(String key) {
        return color(config.getString("messages.prefix", "") + config.getString("messages." + key, key));
    }

    /** Returns a configured message without the prefix. */
    public String rawMessage(String key) {
        return color(config.getString("messages." + key, key));
    }

    private static String color(String s) {
        return s.replaceAll("(?i)&([0-9a-fk-or])", "§$1");
    }

    public boolean debug() {
        return debug;
    }

    public boolean convertChestGuis() {
        return convertChestGuis;
    }

    public boolean includeUnnamed() {
        return includeUnnamed;
    }

    public boolean showLoreInButton() {
        return showLoreInButton;
    }

    public boolean icons() {
        return icons;
    }

    public boolean keepColors() {
        return keepColors;
    }

    public boolean nativeDialogs() {
        return nativeDialogs;
    }

    public boolean fallbackWhenEmpty() {
        return fallbackWhenEmpty;
    }

    public int loreMaxLength() {
        return loreMaxLength;
    }

    public long refreshDelayMillis() {
        return refreshDelayTicks * 50L;
    }

    public String formContent() {
        return formContent;
    }

    public List<String> decorationMaterials() {
        return decorationMaterials;
    }

    public List<String> leftKeywords() {
        return leftKeywords;
    }

    public List<String> rightKeywords() {
        return rightKeywords;
    }
}
