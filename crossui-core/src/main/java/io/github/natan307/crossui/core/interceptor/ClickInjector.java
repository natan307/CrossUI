/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.interceptor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import io.github.natan307.crossui.core.session.UISession;

import java.util.HashMap;
import java.util.Optional;

/**
 * Sends fake client packets into the server pipeline so the plugin that owns the menu reacts as
 * if the player had clicked or closed a real inventory.
 */
public final class ClickInjector {

    /** Since 1.21.5 the click packet carries hashed slots instead of full item stacks. */
    private final boolean hashedSlots;

    public ClickInjector() {
        this.hashedSlots = PacketEvents.getAPI().getServerManager().getVersion()
                .isNewerThanOrEquals(ServerVersion.V_1_21_5);
    }

    /** Injects a normal click on a container slot. {@code button}: 0 = left click, 1 = right click. */
    public void click(UISession session, int slot, int button) {
        WrapperPlayClientClickWindow packet;
        if (hashedSlots) {
            packet = new WrapperPlayClientClickWindow(
                    session.windowId(), session.stateId(), slot, button,
                    WindowClickType.PICKUP, new HashMap<>(), Optional.empty());
        } else {
            packet = new WrapperPlayClientClickWindow(
                    session.windowId(), Optional.of(session.stateId()), slot, button, Optional.of(0),
                    WindowClickType.PICKUP, Optional.of(new HashMap<>()), ItemStack.EMPTY);
        }
        session.user().receivePacket(packet);
    }

    /** Tells the server the player closed the inventory, so the owning plugin can release it. */
    public void close(UISession session) {
        session.user().receivePacket(new WrapperPlayClientCloseWindow(session.windowId()));
    }
}
