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
package io.github.natan307.crossui.core.platform;

import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.UUID;

/** Fallback bridge for Geyser-only servers (no Floodgate installed). */
public final class GeyserAdapter implements BedrockBridge {

    @Override
    public String name() {
        return "Geyser";
    }

    @Override
    public boolean isBedrock(UUID uuid) {
        return GeyserApi.api().isBedrockPlayer(uuid);
    }

    @Override
    public boolean sendForm(UUID uuid, Form form) {
        GeyserConnection connection = GeyserApi.api().connectionByUuid(uuid);
        return connection != null && connection.sendForm(form);
    }
}
