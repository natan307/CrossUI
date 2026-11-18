/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.platform;

import org.geysermc.cumulus.form.Form;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

/** Primary bridge: uses the Floodgate API. */
public final class FloodgateAdapter implements BedrockBridge {

    @Override
    public String name() {
        return "Floodgate";
    }

    @Override
    public boolean isBedrock(UUID uuid) {
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }

    @Override
    public boolean sendForm(UUID uuid, Form form) {
        return FloodgateApi.getInstance().sendForm(uuid, form);
    }
}
