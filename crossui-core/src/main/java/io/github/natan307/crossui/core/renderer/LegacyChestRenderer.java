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
package io.github.natan307.crossui.core.renderer;

import io.github.natan307.crossui.api.builder.ConfirmBuilder;
import io.github.natan307.crossui.api.builder.InputBuilder;
import io.github.natan307.crossui.api.builder.MenuBuilder;
import io.github.natan307.crossui.api.model.UIButton;
import io.github.natan307.crossui.core.CrossUIPlugin;
import io.github.natan307.crossui.core.parser.TextSanitizer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fallback renderer for older Java clients: a classic chest inventory. Written against the
 * Spigot API only (legacy color strings, no Adventure), so it runs on Spigot and Paper alike.
 *
 * <p>For text input there is no way to type into a chest, so the player is asked to type the
 * answer in chat instead.</p>
 */
public final class LegacyChestRenderer implements Listener {

    /** Holder that marks inventories created by CrossUI. */
    private static final class Holder implements InventoryHolder {
        private final Map<Integer, Consumer<Player>> actions = new HashMap<>();
        private Consumer<Player> onClose;
        private boolean closedByAction;
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final CrossUIPlugin plugin;

    public LegacyChestRenderer(CrossUIPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ API builders

    public void menu(Player player, MenuBuilder menu) {
        List<UIButton> buttons = menu.buttons();
        int rows = Math.max(1, Math.min(6, (buttons.size() + 8) / 9));
        Holder holder = new Holder();
        holder.onClose = menu.onClose();
        Inventory inventory = Bukkit.createInventory(holder, rows * 9, trim(TextSanitizer.toBedrock(menu.title())));
        holder.inventory = inventory;

        for (int i = 0; i < buttons.size() && i < rows * 9; i++) {
            UIButton button = buttons.get(i);
            inventory.setItem(i, item(button));
            holder.actions.put(i, button.action());
        }
        open(player, inventory);
    }

    public void confirm(Player player, ConfirmBuilder confirm) {
        Holder holder = new Holder();
        holder.onClose = confirm.onNo();
        Inventory inventory = Bukkit.createInventory(holder, 27, trim(TextSanitizer.toBedrock(confirm.title())));
        holder.inventory = inventory;

        inventory.setItem(11, named(Material.LIME_WOOL, confirm.yesText(), null));
        inventory.setItem(13, named(Material.PAPER, confirm.title(), confirm.body()));
        inventory.setItem(15, named(Material.RED_WOOL, confirm.noText(), null));
        holder.actions.put(11, confirm.onYes());
        holder.actions.put(15, confirm.onNo());
        open(player, inventory);
    }

    public void input(Player player, InputBuilder input) {
        plugin.chatInput().request(player, input);
    }

    // ------------------------------------------------------------------ events

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder holder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            return;
        }
        Consumer<Player> action = holder.actions.get(event.getSlot());
        if (action == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        holder.closedByAction = true;
        player.closeInventory();
        action.accept(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder holder)) {
            return;
        }
        if (!holder.closedByAction && holder.onClose != null && event.getPlayer() instanceof Player player) {
            holder.onClose.accept(player);
        }
    }

    // ------------------------------------------------------------------ helpers

    private void open(Player player, Inventory inventory) {
        plugin.scheduler().runForPlayer(player, () -> player.openInventory(inventory));
    }

    private static String trim(String title) {
        return title.length() <= 32 ? title : title.substring(0, 32);
    }

    private static ItemStack item(UIButton button) {
        Material material = Material.PAPER;
        String icon = button.icon();
        if (icon != null && !icon.contains(":")) {
            Material parsed = Material.matchMaterial(icon.toUpperCase(Locale.ROOT));
            if (parsed != null && parsed.isItem()) {
                material = parsed;
            }
        }
        return named(material, button.title(), button.subtext());
    }

    private static ItemStack named(Material material, String name, String lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextSanitizer.toBedrock("&r" + name));
            if (lore != null && !lore.isEmpty()) {
                List<String> lines = new ArrayList<>();
                lines.add(TextSanitizer.toBedrock("&7" + lore));
                meta.setLore(lines);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
