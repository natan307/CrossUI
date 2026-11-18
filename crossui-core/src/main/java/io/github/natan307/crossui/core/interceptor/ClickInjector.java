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
