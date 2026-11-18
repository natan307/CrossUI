/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.renderer;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.ConfirmationDialog;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import io.github.natan307.crossui.api.builder.ConfirmBuilder;
import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.api.builder.MenuBuilder;
import io.github.natan307.crossui.api.model.UIButton;
import io.github.natan307.crossui.core.CrossUIPlugin;
import io.github.natan307.crossui.core.parser.TextSanitizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Shows Java native dialogs (Minecraft 1.21.6+) by sending the SHOW_DIALOG packet through
 * PacketEvents, and routes the {@code custom_click_action} responses back to the callbacks.
 * Pure protocol level, so it works on Spigot as well as Paper, Purpur and Folia.
 */
public final class JavaDialogRenderer {

    private static final String NAMESPACE = "crossui";
    private static final long EXPIRY_MILLIS = 10 * 60 * 1000L;
    private static final int BUTTON_WIDTH = 200;

    private record Pending(UUID player, String group, Consumer<NBT> handler, long createdAt) {
    }

    private final CrossUIPlugin plugin;
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    public JavaDialogRenderer(CrossUIPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ API builders

    public boolean menu(Player player, MenuBuilder menu) {
        UUID uuid = player.getUniqueId();
        beginFor(uuid);
        String group = newGroup();

        List<ActionButton> actions = new ArrayList<>();
        int index = 0;
        for (UIButton button : menu.buttons()) {
            Component label = TextSanitizer.fromLegacy(button.title());
            if (button.subtext() != null && !button.subtext().isEmpty()) {
                label = Component.text().append(label).append(Component.newline())
                        .append(TextSanitizer.fromLegacy(button.subtext()).color(NamedTextColor.GRAY)).build();
            }
            String id = register(uuid, group, index++, payload ->
                    plugin.scheduler().runForPlayer(player, () -> button.action().accept(player)));
            actions.add(button(label, id));
        }

        List<DialogBody> body = new ArrayList<>();
        if (!menu.content().isEmpty()) {
            body.add(message(menu.content()));
        }
        CommonDialogData common = common(menu.title(), body, List.of());

        Dialog dialog;
        if (actions.isEmpty()) {
            dialog = new NoticeDialog(common, NoticeDialog.DEFAULT_ACTION);
        } else {
            ActionButton exit = null;
            if (menu.onClose() != null) {
                String exitId = register(uuid, group, "exit", payload ->
                        plugin.scheduler().runForPlayer(player, () -> menu.onClose().accept(player)));
                exit = button(Component.text("Close"), exitId);
            }
            dialog = new MultiActionDialog(common, actions, exit, 1);
        }
        return show(player, dialog);
    }

    public boolean input(Player player, InputBuilder input) {
        UUID uuid = player.getUniqueId();
        beginFor(uuid);
        String group = newGroup();

        String submitId = register(uuid, group, "submit", payload -> {
            String value = "";
            if (payload instanceof NBTCompound compound) {
                NBTString tag = compound.getStringTagOrNull("value");
                if (tag != null) {
                    value = tag.getValue();
                }
            }
            String text = value;
            plugin.scheduler().runForPlayer(player, () -> input.onSubmit().accept(player, text));
        });
        String cancelId = register(uuid, group, "cancel", payload -> {
            if (input.onCancel() != null) {
                plugin.scheduler().runForPlayer(player, () -> input.onCancel().accept(player));
            }
        });

        List<DialogBody> body = new ArrayList<>();
        if (!input.placeholder().isEmpty()) {
            body.add(message("&7" + input.placeholder()));
        }

        String initial = input.defaultValue();
        TextInputControl control = new TextInputControl(
                BUTTON_WIDTH, TextSanitizer.fromLegacy(input.label()), true,
                initial, Math.max(256, initial.length()), null);
        CommonDialogData common = common(input.title(), body, List.of(new Input("value", control)));

        Dialog dialog = new ConfirmationDialog(
                common,
                button(Component.text("Submit"), submitId),
                button(Component.text("Cancel"), cancelId));
        return show(player, dialog);
    }

    public boolean confirm(Player player, ConfirmBuilder confirm) {
        UUID uuid = player.getUniqueId();
        beginFor(uuid);
        String group = newGroup();

        String yesId = register(uuid, group, "yes", payload ->
                plugin.scheduler().runForPlayer(player, () -> confirm.onYes().accept(player)));
        String noId = register(uuid, group, "no", payload ->
                plugin.scheduler().runForPlayer(player, () -> confirm.onNo().accept(player)));

        List<DialogBody> body = new ArrayList<>();
        if (!confirm.body().isEmpty()) {
            body.add(message(confirm.body()));
        }
        Dialog dialog = new ConfirmationDialog(
                common(confirm.title(), body, List.of()),
                button(TextSanitizer.fromLegacy(confirm.yesText()), yesId),
                button(TextSanitizer.fromLegacy(confirm.noText()), noId));
        return show(player, dialog);
    }

    // ------------------------------------------------------------------ responses

    /** Called from the packet listener when the client answered one of our dialogs. */
    public void handleClick(UUID uuid, String path, NBT payload) {
        Pending entry = pending.get(path);
        if (entry == null || !entry.player().equals(uuid)) {
            return;
        }
        // One answer per dialog: drop every button of the same dialog.
        pending.values().removeIf(p -> p.group().equals(entry.group()));
        try {
            entry.handler().accept(payload);
        } catch (Exception e) {
            plugin.getLogger().warning("Dialog callback failed: " + e);
        }
    }

    /** Forgets all dialogs of a player (called on quit). */
    public void clear(UUID uuid) {
        pending.values().removeIf(p -> p.player().equals(uuid));
    }

    public void clearAll() {
        pending.clear();
    }

    // ------------------------------------------------------------------ helpers

    private void beginFor(UUID uuid) {
        long now = System.currentTimeMillis();
        pending.values().removeIf(p -> p.player().equals(uuid) || now - p.createdAt() > EXPIRY_MILLIS);
    }

    private static String newGroup() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String register(UUID uuid, String group, Object suffix, Consumer<NBT> handler) {
        String path = "d/" + group + "/" + suffix;
        pending.put(path, new Pending(uuid, group, handler, System.currentTimeMillis()));
        return path;
    }

    private static ActionButton button(Component label, String path) {
        return new ActionButton(
                new CommonButtonData(label, null, BUTTON_WIDTH),
                new DynamicCustomAction(new ResourceLocation(NAMESPACE, path), null));
    }

    private static DialogBody message(String legacyText) {
        return new PlainMessageDialogBody(new PlainMessage(TextSanitizer.fromLegacy(legacyText), 300));
    }

    private static CommonDialogData common(String title, List<DialogBody> body, List<Input> inputs) {
        return new CommonDialogData(
                TextSanitizer.fromLegacy(title), null, true, false, DialogAction.CLOSE, body, inputs);
    }

    private boolean show(Player player, Dialog dialog) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) {
            clear(player.getUniqueId());
            return false;
        }
        user.sendPacket(new WrapperPlayServerShowDialog(dialog));
        return true;
    }

}
