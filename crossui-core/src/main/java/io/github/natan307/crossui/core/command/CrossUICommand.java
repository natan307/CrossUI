/*
 * CrossUI - https://github.com/natan307/CrossUI
 * Copyright (c) 2026 natan307
 * Licensed under the MIT License. See the LICENSE file in the project root.
 */
package io.github.natan307.crossui.core.command;

import io.github.natan307.crossui.api.CrossUI;
import io.github.natan307.crossui.core.CrossUIPlugin;
import io.github.natan307.crossui.core.internal.Origin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Handles {@code /crossui reload} and {@code /crossui testgui}. */
public final class CrossUICommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "testgui");

    private final CrossUIPlugin plugin;

    public CrossUICommand(CrossUIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("crossui.admin")) {
            sender.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.config().message("usage"));
            sender.sendMessage(plugin.config().rawMessage("prefix") + Origin.WIKI_URL);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.config().load();
                plugin.platforms().clear();
                sender.sendMessage(plugin.config().message("reload-success"));
            }
            case "testgui" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.config().message("player-only"));
                    return true;
                }
                openTest(player);
            }
            default -> sender.sendMessage(plugin.config().message("unknown-subcommand"));
        }
        return true;
    }

    private void openTest(Player player) {
        var cfg = plugin.config();
        String platform = CrossUI.platform(player).name();

        CrossUI.menu(player)
                .title(cfg.rawMessage("testgui-title"))
                .content(cfg.rawMessage("testgui-content").replace("%platform%", platform))
                .button(cfg.rawMessage("testgui-input-button"), p ->
                        CrossUI.input(p)
                                .title(cfg.rawMessage("testgui-input-title"))
                                .label(cfg.rawMessage("testgui-input-label"))
                                .placeholder(cfg.rawMessage("testgui-input-placeholder"))
                                .onSubmit((pl, text) -> pl.sendMessage(
                                        cfg.rawMessage("prefix")
                                                + cfg.rawMessage("testgui-input-result").replace("%text%", text)))
                                .open())
                .button(cfg.rawMessage("testgui-confirm-button"), p ->
                        CrossUI.confirm(p)
                                .title(cfg.rawMessage("testgui-confirm-title"))
                                .body(cfg.rawMessage("testgui-confirm-body"))
                                .onYes(pl -> pl.sendMessage(cfg.rawMessage("prefix") + cfg.rawMessage("testgui-confirm-yes")))
                                .onNo(pl -> pl.sendMessage(cfg.rawMessage("prefix") + cfg.rawMessage("testgui-confirm-no")))
                                .open())
                .button(cfg.rawMessage("testgui-close-button"), p -> { })
                .open();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("crossui.admin")) {
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    out.add(sub);
                }
            }
        }
        return out;
    }
}
