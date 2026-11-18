/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.platform;

import org.geysermc.cumulus.form.Form;

import java.util.UUID;

/** Abstraction over the library that delivers Bedrock forms (Floodgate or Geyser). */
public interface BedrockBridge {

    /** Human readable bridge name, used for logs and metrics. */
    String name();

    /** Whether the given player is connected through Bedrock Edition. Thread-safe. */
    boolean isBedrock(UUID uuid);

    /** Sends a form to the player. Returns {@code false} when it could not be delivered. Thread-safe. */
    boolean sendForm(UUID uuid, Form form);
}
