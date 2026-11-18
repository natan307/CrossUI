/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.session;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.natan307.crossui.core.parser.ParsedButton;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/** State of one intercepted chest menu that is being shown to a Bedrock player as a form. */
public final class UISession {

    public enum State {
        /** OPEN_WINDOW was held back, waiting for the item list. */
        AWAITING_ITEMS,
        /** A form is currently displayed. */
        SHOWN,
        /** A click was injected; waiting for the menu to refresh or close. */
        CLICKED,
        /** Not convertible: packets flow to the client untouched. */
        PASSTHROUGH
    }

    private final User user;
    private final UUID playerId;
    private final int windowId;
    private final int menuType;
    private final Component rawTitle;
    private final String title;
    private final AtomicBoolean renderScheduled = new AtomicBoolean();

    private volatile State state = State.AWAITING_ITEMS;
    private volatile List<ItemStack> slots = List.of();
    private volatile List<ParsedButton> buttons = List.of();
    private volatile int stateId;
    private volatile boolean openResent;

    public UISession(User user, int windowId, int menuType, Component rawTitle, String title) {
        this.user = user;
        this.playerId = user.getUUID();
        this.windowId = windowId;
        this.menuType = menuType;
        this.rawTitle = rawTitle;
        this.title = title;
    }

    public User user() {
        return user;
    }

    public UUID playerId() {
        return playerId;
    }

    public int windowId() {
        return windowId;
    }

    /** Container menu type id (0 = 9x1 ... 5 = 9x6). */
    public int menuType() {
        return menuType;
    }

    public int rows() {
        return menuType + 1;
    }

    public Component rawTitle() {
        return rawTitle;
    }

    public String title() {
        return title;
    }

    public State state() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<ItemStack> slots() {
        return slots;
    }

    public void setSlots(List<ItemStack> slots) {
        this.slots = new ArrayList<>(slots);
    }

    public synchronized void setSlot(int index, ItemStack item) {
        if (index < 0) {
            return;
        }
        List<ItemStack> copy = new ArrayList<>(slots);
        while (copy.size() <= index) {
            copy.add(ItemStack.EMPTY);
        }
        copy.set(index, item);
        this.slots = copy;
    }

    public List<ParsedButton> buttons() {
        return buttons;
    }

    public void setButtons(List<ParsedButton> buttons) {
        this.buttons = buttons;
    }

    public int stateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public AtomicBoolean renderScheduled() {
        return renderScheduled;
    }

    public boolean openResent() {
        return openResent;
    }

    public void markOpenResent() {
        this.openResent = true;
    }
}
