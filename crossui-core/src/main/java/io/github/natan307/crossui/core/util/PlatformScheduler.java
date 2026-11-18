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
package io.github.natan307.crossui.core.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Runs tasks on the correct thread for Spigot, Paper, Purpur and Folia.
 *
 * <p>The plugin is compiled against spigot-api, which has no Folia scheduler classes, so the
 * Folia schedulers are reached through reflection. On non-Folia servers the normal Bukkit
 * scheduler is used.</p>
 */
public final class PlatformScheduler {

    private final Plugin plugin;
    private final boolean folia;

    private Method entityGetScheduler;
    private Method entityRun;
    private Method globalGetScheduler;
    private Method globalRun;

    public PlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.folia = classExists("io.papermc.paper.threadedregions.RegionizedServer");
        if (folia) {
            try {
                entityGetScheduler = Class.forName("org.bukkit.entity.Entity").getMethod("getScheduler");
                Class<?> entityScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                entityRun = entityScheduler.getMethod("run", Plugin.class, Consumer.class, Runnable.class);

                globalGetScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
                Class<?> globalScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
                globalRun = globalScheduler.getMethod("run", Plugin.class, Consumer.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia detected but its schedulers could not be resolved", e);
            }
        }
    }

    public boolean isFolia() {
        return folia;
    }

    /** Runs the task on the thread that owns the given player. */
    public void runForPlayer(Player player, Runnable task) {
        try {
            if (!folia) {
                Bukkit.getScheduler().runTask(plugin, task);
                return;
            }
            Object scheduler = entityGetScheduler.invoke(player);
            Consumer<Object> consumer = ignored -> task.run();
            entityRun.invoke(scheduler, plugin, consumer, null);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not schedule a task for " + player.getName(), e);
        }
    }

    /** Runs the task on the global (main) thread. */
    public void runGlobal(Runnable task) {
        try {
            if (!folia) {
                Bukkit.getScheduler().runTask(plugin, task);
                return;
            }
            Object scheduler = globalGetScheduler.invoke(null);
            Consumer<Object> consumer = ignored -> task.run();
            globalRun.invoke(scheduler, plugin, consumer);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not schedule a global task", e);
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
