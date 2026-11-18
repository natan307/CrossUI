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

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCustomClickAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.natan307.crossui.api.platform.ClientPlatform;
import io.github.natan307.crossui.core.CrossUIPlugin;
import io.github.natan307.crossui.core.parser.ParsedButton;
import io.github.natan307.crossui.core.session.UISession;
import io.github.natan307.crossui.core.session.UISession.State;
import org.geysermc.cumulus.form.Form;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Network-level interceptor. Works on Netty threads and never touches world state, so it behaves
 * the same on Spigot, Paper, Purpur and Folia.
 *
 * <p>For Bedrock players, chest GUIs sent by other plugins are held back and re-shown as forms.
 * Choices are injected back into the server as ordinary click packets.</p>
 */
public final class PacketInterceptor extends PacketListenerAbstract {

    /** Menu type ids 0..5 are generic_9x1 .. generic_9x6 (the chest family). */
    private static final int MAX_CHEST_TYPE = 5;

    private final CrossUIPlugin plugin;

    public PacketInterceptor(CrossUIPlugin plugin) {
        super(PacketListenerPriority.HIGH);
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ client -> server

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CUSTOM_CLICK_ACTION) {
            return;
        }
        try {
            WrapperPlayClientCustomClickAction wrapper = new WrapperPlayClientCustomClickAction(event);
            ResourceLocation id = wrapper.getId();
            if (!"crossui".equals(id.getNamespace())) {
                return;
            }
            event.setCancelled(true);
            User user = event.getUser();
            if (user != null && user.getUUID() != null) {
                plugin.dialogs().handleClick(user.getUUID(), id.getKey(), wrapper.getPayload());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to handle a dialog response", e);
        }
    }

    // ------------------------------------------------------------------ server -> client

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon type = event.getPacketType();
        boolean relevant = type == PacketType.Play.Server.OPEN_WINDOW
                || type == PacketType.Play.Server.WINDOW_ITEMS
                || type == PacketType.Play.Server.SET_SLOT
                || type == PacketType.Play.Server.CLOSE_WINDOW;
        if (!relevant || !plugin.config().convertChestGuis() || plugin.bridge() == null) {
            return;
        }

        User user = event.getUser();
        if (user == null || user.getUUID() == null) {
            return;
        }
        if (plugin.platforms().resolve(user.getUUID(), user.getClientVersion()) != ClientPlatform.BEDROCK) {
            return;
        }

        try {
            if (type == PacketType.Play.Server.OPEN_WINDOW) {
                handleOpen(event, user);
            } else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                handleItems(event, user);
            } else if (type == PacketType.Play.Server.SET_SLOT) {
                handleSetSlot(event, user);
            } else {
                handleServerClose(event, user);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "CrossUI failed to process a menu packet; letting it through", e);
            plugin.sessions().remove(user.getUUID());
        }
    }

    private void handleOpen(PacketSendEvent event, User user) {
        UUID id = user.getUUID();
        WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(event);
        UISession existing = plugin.sessions().get(id);

        // Our own re-sent OPEN_WINDOW for a menu that could not be converted.
        if (existing != null && existing.state() == State.PASSTHROUGH
                && existing.windowId() == wrapper.getContainerId() && !existing.openResent()) {
            existing.markOpenResent();
            return;
        }

        int menuType = wrapper.getType();
        if (menuType < 0 || menuType > MAX_CHEST_TYPE) {
            if (existing != null) {
                plugin.sessions().remove(id, existing);
            }
            return;
        }

        String title = plugin.text().plain(wrapper.getTitle());
        if (!plugin.config().isTitleAllowed(title)) {
            debug("Skipping menu '" + title + "' (blacklist/whitelist)");
            if (existing != null) {
                plugin.sessions().remove(id, existing);
            }
            return;
        }

        event.setCancelled(true);
        plugin.sessions().put(id, new UISession(user, wrapper.getContainerId(), menuType, wrapper.getTitle(), title));
        debug("Holding menu '" + title + "' for " + id);
    }

    private void handleItems(PacketSendEvent event, User user) {
        UUID id = user.getUUID();
        WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event);
        UISession session = plugin.sessions().get(id);
        if (session == null || session.windowId() != wrapper.getWindowId()) {
            return;
        }

        switch (session.state()) {
            case PASSTHROUGH -> {
                // untouched
            }
            case AWAITING_ITEMS -> {
                session.setSlots(wrapper.getItems());
                session.setStateId(wrapper.getStateId());
                List<ParsedButton> buttons = plugin.filter().parse(session.slots(), session.rows() * 9);

                if (buttons.isEmpty() && plugin.config().fallbackWhenEmpty()) {
                    debug("Menu '" + session.title() + "' has no buttons; showing it normally");
                    session.setState(State.PASSTHROUGH);
                    user.sendPacket(new WrapperPlayServerOpenWindow(
                            session.windowId(), session.menuType(), session.rawTitle()));
                    return; // the original WINDOW_ITEMS continues to the client
                }

                event.setCancelled(true);
                session.setButtons(buttons);
                session.setState(State.SHOWN);
                show(session);
            }
            case SHOWN, CLICKED -> {
                event.setCancelled(true);
                session.setSlots(wrapper.getItems());
                session.setStateId(wrapper.getStateId());
                if (session.state() == State.CLICKED) {
                    scheduleRender(session);
                }
            }
        }
    }

    private void handleSetSlot(PacketSendEvent event, User user) {
        WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);
        UISession session = plugin.sessions().get(user.getUUID());
        if (session == null || session.windowId() != wrapper.getWindowId() || session.state() == State.PASSTHROUGH) {
            return;
        }
        event.setCancelled(true);
        session.setSlot(wrapper.getSlot(), wrapper.getItem());
        session.setStateId(wrapper.getStateId());
        if (session.state() == State.CLICKED) {
            scheduleRender(session);
        }
    }

    private void handleServerClose(PacketSendEvent event, User user) {
        UUID id = user.getUUID();
        WrapperPlayServerCloseWindow wrapper = new WrapperPlayServerCloseWindow(event);
        UISession session = plugin.sessions().get(id);
        if (session == null || session.windowId() != wrapper.getWindowId()) {
            return;
        }
        if (session.state() != State.PASSTHROUGH) {
            event.setCancelled(true);
        }
        plugin.sessions().remove(id, session);
    }

    // ------------------------------------------------------------------ form flow

    private void show(UISession session) {
        Form form = plugin.bedrockRenderer().sessionForm(
                session,
                index -> onSelect(session, index),
                () -> onClosed(session));
        if (!plugin.bridge().sendForm(session.playerId(), form)) {
            debug("Could not deliver the form to " + session.playerId());
        }
    }

    private boolean isLive(UISession session) {
        return plugin.sessions().get(session.playerId()) == session;
    }

    private void onSelect(UISession session, int index) {
        if (!isLive(session) || session.state() != State.SHOWN) {
            return;
        }
        List<ParsedButton> buttons = session.buttons();
        if (index < 0 || index >= buttons.size()) {
            return;
        }
        ParsedButton button = buttons.get(index);

        if (button.dual()) {
            Form chooser = plugin.bedrockRenderer().dualForm(
                    button,
                    left -> {
                        if (isLive(session) && session.state() == State.SHOWN) {
                            click(session, button, left ? 0 : 1);
                        }
                    },
                    () -> {
                        if (isLive(session) && session.state() == State.SHOWN) {
                            show(session);
                        }
                    });
            plugin.bridge().sendForm(session.playerId(), chooser);
        } else {
            click(session, button, 0);
        }
    }

    private void click(UISession session, ParsedButton button, int mouseButton) {
        session.setState(State.CLICKED);
        plugin.injector().click(session, button.slot(), mouseButton);
        scheduleRender(session);
    }

    private void onClosed(UISession session) {
        if (!isLive(session) || session.state() != State.SHOWN) {
            return;
        }
        plugin.sessions().remove(session.playerId(), session);
        plugin.injector().close(session);
    }

    /** Waits briefly for the menu to refresh, then shows the updated form (or ends the session). */
    private void scheduleRender(UISession session) {
        if (!session.renderScheduled().compareAndSet(false, true)) {
            return;
        }
        plugin.executor().schedule(() -> {
            session.renderScheduled().set(false);
            try {
                rerender(session);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to refresh a converted menu", e);
            }
        }, plugin.config().refreshDelayMillis(), TimeUnit.MILLISECONDS);
    }

    private void rerender(UISession session) {
        if (!isLive(session) || session.state() != State.CLICKED) {
            return;
        }
        List<ParsedButton> buttons = plugin.filter().parse(session.slots(), session.rows() * 9);
        if (buttons.isEmpty()) {
            plugin.sessions().remove(session.playerId(), session);
            plugin.injector().close(session);
            return;
        }
        session.setButtons(buttons);
        session.setState(State.SHOWN);
        show(session);
    }

    private void debug(String message) {
        if (plugin.config().debug()) {
            plugin.getLogger().info("[debug] " + message);
        }
    }
}
