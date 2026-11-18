/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
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
