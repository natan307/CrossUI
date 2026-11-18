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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns Adventure components and legacy color strings into form-friendly text.
 *
 * <p>This class only uses the {@code net.kyori.adventure} API classes that PacketEvents ships
 * (and Paper provides natively). It deliberately avoids the Adventure serializer modules, which
 * PacketEvents relocates, so it behaves the same on Spigot and Paper.</p>
 */
public final class TextSanitizer {

    private static final Map<NamedTextColor, Character> CODES = new HashMap<>();
    private static final Map<Character, NamedTextColor> COLORS = new HashMap<>();

    static {
        register(NamedTextColor.BLACK, '0');
        register(NamedTextColor.DARK_BLUE, '1');
        register(NamedTextColor.DARK_GREEN, '2');
        register(NamedTextColor.DARK_AQUA, '3');
        register(NamedTextColor.DARK_RED, '4');
        register(NamedTextColor.DARK_PURPLE, '5');
        register(NamedTextColor.GOLD, '6');
        register(NamedTextColor.GRAY, '7');
        register(NamedTextColor.DARK_GRAY, '8');
        register(NamedTextColor.BLUE, '9');
        register(NamedTextColor.GREEN, 'a');
        register(NamedTextColor.AQUA, 'b');
        register(NamedTextColor.RED, 'c');
        register(NamedTextColor.LIGHT_PURPLE, 'd');
        register(NamedTextColor.YELLOW, 'e');
        register(NamedTextColor.WHITE, 'f');
    }

    private static final Pattern SECTION_CODES = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HEX_AMP = Pattern.compile("(?i)&#[0-9a-f]{6}");
    private static final Pattern AMP_CODE = Pattern.compile("(?i)&([0-9a-fk-or])");

    private static void register(NamedTextColor color, char code) {
        CODES.put(color, code);
        COLORS.put(code, color);
    }

    /** Plain text of a component: no colors, collapsed whitespace. */
    public String plain(Component component) {
        return clean(render(component, false));
    }

    /** Text of a component with basic {@code §} color codes (RGB reduced to the nearest basic color). */
    public String colored(Component component) {
        return clean(render(component, true));
    }

    /** Removes {@code §} codes and collapses whitespace. */
    public String clean(String text) {
        String stripped = SECTION_CODES.matcher(text).replaceAll("");
        return WHITESPACE.matcher(stripped).replaceAll(" ").trim();
    }

    /** True when the text has no letters or digits (empty, spaces, or only separator symbols). */
    public boolean isBlank(String text) {
        return text.replaceAll("[^\\p{L}\\p{N}]", "").isEmpty();
    }

    public static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…";
    }

    // ------------------------------------------------------------------ rendering

    private static String render(Component component, boolean colors) {
        StringBuilder sb = new StringBuilder();
        render(component, Style.empty(), colors, sb);
        return sb.toString();
    }

    private static void render(Component component, Style inherited, boolean colors, StringBuilder sb) {
        Style style = inherited.merge(component.style());

        String text = null;
        if (component instanceof TextComponent t) {
            text = t.content();
        } else if (component instanceof TranslatableComponent tr) {
            text = tr.fallback() != null ? tr.fallback() : tr.key();
        }

        if (text != null && !text.isEmpty()) {
            boolean marked = false;
            if (colors) {
                TextColor color = style.color();
                if (color != null) {
                    NamedTextColor nearest = color instanceof NamedTextColor n ? n : NamedTextColor.nearestTo(color);
                    sb.append('§').append(CODES.getOrDefault(nearest, 'f'));
                    marked = true;
                }
                if (style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE) {
                    sb.append("§l");
                    marked = true;
                }
            }
            sb.append(text);
            if (marked) {
                sb.append("§r");
            }
        }

        for (Component child : component.children()) {
            render(child, style, colors, sb);
        }
    }

    // ------------------------------------------------------------ legacy strings

    /** Converts {@code &} color codes (and {@code &#rrggbb}) into {@code §} codes Bedrock understands. */
    public static String toBedrock(String text) {
        String noHex = HEX_AMP.matcher(text).replaceAll("");
        return AMP_CODE.matcher(noHex).replaceAll("§$1");
    }

    /** Removes all {@code &} and {@code §} formatting codes. */
    public static String strip(String text) {
        String noHex = HEX_AMP.matcher(text).replaceAll("");
        String noAmp = AMP_CODE.matcher(noHex).replaceAll("");
        return SECTION_CODES.matcher(noAmp).replaceAll("");
    }

    /** Parses a legacy color string ({@code &} or {@code §}, plus {@code &#rrggbb}) into a Component. */
    public static Component fromLegacy(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        TextComponent.Builder root = Component.text();
        StringBuilder buffer = new StringBuilder();
        TextColor color = null;
        Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if ((ch == '&' || ch == '§') && i + 1 < input.length()) {
                char next = Character.toLowerCase(input.charAt(i + 1));

                if (next == '#' && isHex(input, i + 2)) {
                    flush(root, buffer, color, decorations);
                    color = TextColor.color(Integer.parseInt(input.substring(i + 2, i + 8), 16));
                    decorations.clear();
                    i += 7;
                    continue;
                }

                NamedTextColor named = COLORS.get(next);
                if (named != null) {
                    flush(root, buffer, color, decorations);
                    color = named;
                    decorations.clear();
                    i++;
                    continue;
                }

                TextDecoration decoration = switch (next) {
                    case 'l' -> TextDecoration.BOLD;
                    case 'o' -> TextDecoration.ITALIC;
                    case 'n' -> TextDecoration.UNDERLINED;
                    case 'm' -> TextDecoration.STRIKETHROUGH;
                    case 'k' -> TextDecoration.OBFUSCATED;
                    default -> null;
                };
                if (decoration != null) {
                    flush(root, buffer, color, decorations);
                    decorations.add(decoration);
                    i++;
                    continue;
                }
                if (next == 'r') {
                    flush(root, buffer, color, decorations);
                    color = null;
                    decorations.clear();
                    i++;
                    continue;
                }
            }
            buffer.append(ch);
        }
        flush(root, buffer, color, decorations);
        return root.build();
    }

    private static boolean isHex(String s, int start) {
        if (start + 6 > s.length()) {
            return false;
        }
        for (int i = start; i < start + 6; i++) {
            if (Character.digit(s.charAt(i), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static void flush(TextComponent.Builder root, StringBuilder buffer, TextColor color, Set<TextDecoration> decorations) {
        if (buffer.isEmpty()) {
            return;
        }
        Component part = Component.text(buffer.toString(), color);
        for (TextDecoration decoration : decorations) {
            part = part.decoration(decoration, true);
        }
        root.append(part);
        buffer.setLength(0);
    }
}
