/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.api.platform;

/** The UI family a player's client is able to display. */
public enum ClientPlatform {
    /** Bedrock Edition client (via Geyser / Floodgate): rendered as Bedrock Forms. */
    BEDROCK,
    /** Java client 1.21.6 or newer: rendered as native Dialogs. */
    JAVA_DIALOG,
    /** Older Java client (for example through ViaVersion): rendered as inventory GUIs. */
    JAVA_LEGACY
}
