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

import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds out which skin a player head shows, so the button can use a picture of that head. */
public final class HeadTexture {

    private static final Pattern TEXTURE = Pattern.compile("texture/([0-9a-fA-F]{16,128})");

    private HeadTexture() {
    }

    /**
     * @return the skin texture hash of the head, or the owner's UUID when the head only names a player,
     *         or {@code null} when the profile says nothing usable
     */
    public static String valueOf(ItemProfile profile) {
        if (profile == null) {
            return null;
        }
        try {
            for (ItemProfile.Property property : profile.getProperties()) {
                if (property != null && "textures".equals(property.getName()) && property.getValue() != null) {
                    String hash = hashOf(property.getValue());
                    if (hash != null) {
                        return hash;
                    }
                }
            }
            if (profile.getId() != null) {
                return profile.getId().toString().replace("-", "");
            }
        } catch (RuntimeException ignored) {
            // a malformed profile just means a head without a picture
        }
        return null;
    }

    static String hashOf(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64.trim()), StandardCharsets.UTF_8);
            Matcher m = TEXTURE.matcher(json);
            return m.find() ? m.group(1).toLowerCase(java.util.Locale.ROOT) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
