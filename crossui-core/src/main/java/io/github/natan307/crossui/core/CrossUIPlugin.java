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
package io.github.natan307.crossui.core;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.natan307.crossui.api.CrossUI;
import io.github.natan307.crossui.api.CrossUIProvider;
import io.github.natan307.crossui.api.platform.ClientPlatform;
import io.github.natan307.crossui.core.command.CrossUICommand;
import io.github.natan307.crossui.core.config.BridgeConfig;
import io.github.natan307.crossui.core.internal.Origin;
import io.github.natan307.crossui.core.interceptor.ClickInjector;
import io.github.natan307.crossui.core.interceptor.PacketInterceptor;
import io.github.natan307.crossui.core.parser.InventoryFilter;
import io.github.natan307.crossui.core.parser.TextSanitizer;
import io.github.natan307.crossui.core.platform.BedrockBridge;
import io.github.natan307.crossui.core.platform.FloodgateAdapter;
import io.github.natan307.crossui.core.platform.GeyserAdapter;
import io.github.natan307.crossui.core.platform.PlatformResolver;
import io.github.natan307.crossui.core.renderer.BedrockFormRenderer;
import io.github.natan307.crossui.core.renderer.ChatInputService;
import io.github.natan307.crossui.core.renderer.JavaDialogRenderer;
import io.github.natan307.crossui.core.renderer.LegacyChestRenderer;
import io.github.natan307.crossui.core.session.SessionManager;
import io.github.natan307.crossui.core.util.PlatformScheduler;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CrossUI main class.
 *
 * <p>Compiled against spigot-api and built on a bundled PacketEvents, so one jar runs on Spigot, Paper,
 * Purpur and Folia. No Bukkit scheduler is used for packet work; Folia-safe scheduling lives in
 * {@link PlatformScheduler}.</p>
 */
public final class CrossUIPlugin extends JavaPlugin implements Listener {

    private BridgeConfig config;
    private PlatformScheduler scheduler;
    private PlatformResolver platforms;
    private SessionManager sessions;
    private TextSanitizer text;
    private InventoryFilter filter;
    private ClickInjector injector;
    private BedrockFormRenderer bedrockRenderer;
    private JavaDialogRenderer dialogs;
    private LegacyChestRenderer legacyRenderer;
    private ChatInputService chatInput;
    private PacketInterceptor interceptor;
    private CrossUIProviderImpl service;
    private ScheduledExecutorService executor;
    private volatile BedrockBridge bridge;

    @Override
    public void onLoad() {
        // PacketEvents is bundled (relocated) inside this jar, so CrossUI owns its own instance.
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        saveDefaultConfig();

        this.scheduler = new PlatformScheduler(this);
        this.config = new BridgeConfig(this);
        this.config.load();

        this.text = new TextSanitizer();
        this.filter = new InventoryFilter(config, text);
        this.sessions = new SessionManager();
        this.injector = new ClickInjector();
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "CrossUI-Refresh");
            thread.setDaemon(true);
            return thread;
        });

        this.bridge = detectBridge();
        this.platforms = new PlatformResolver(() -> bridge, config);

        this.bedrockRenderer = new BedrockFormRenderer(this, config);
        this.dialogs = new JavaDialogRenderer(this);
        this.legacyRenderer = new LegacyChestRenderer(this);
        this.chatInput = new ChatInputService(this);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(legacyRenderer, this);
        getServer().getPluginManager().registerEvents(chatInput, this);

        this.interceptor = new PacketInterceptor(this);
        PacketEvents.getAPI().getEventManager().registerListener(interceptor);

        PluginCommand command = getCommand("crossui");
        if (command != null) {
            CrossUICommand handler = new CrossUICommand(this);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        this.service = new CrossUIProviderImpl(this);
        // 1. Bukkit ServicesManager (same pattern as Vault and LuckPerms)
        getServer().getServicesManager().register(CrossUIProvider.class, service, this, ServicePriority.Normal);
        // 2. Static facade
        CrossUI.setProvider(service);

        setupMetrics();

        getLogger().info("Enabled on " + getServer().getName()
                + (scheduler.isFolia() ? " (Folia)" : "")
                + " | Bedrock bridge: " + (bridge == null ? "none" : bridge.name())
                + " | Native dialogs: " + (platforms.serverSupportsDialogs() ? "available" : "unavailable (server < 1.21.6)"));
        getLogger().info("Wiki: " + Origin.WIKI_URL);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        CrossUI.clearProvider();
        if (interceptor != null && PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(interceptor);
        }
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (sessions != null) {
            sessions.clear();
        }
        if (dialogs != null) {
            dialogs.clearAll();
        }
        if (chatInput != null) {
            chatInput.clear();
        }
        if (platforms != null) {
            platforms.clear();
        }
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        sessions.remove(id);
        dialogs.clear(id);
        platforms.invalidate(id);
    }

    // ------------------------------------------------------------------ setup

    private BedrockBridge detectBridge() {
        try {
            if (getServer().getPluginManager().isPluginEnabled("floodgate")) {
                return new FloodgateAdapter();
            }
            if (getServer().getPluginManager().isPluginEnabled("Geyser-Spigot")) {
                return new GeyserAdapter();
            }
        } catch (LinkageError e) {
            getLogger().warning("Bedrock bridge classes could not be loaded: " + e);
        }
        return null;
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this, Origin.BSTATS_ID);
        metrics.addCustomChart(new SimplePie("bedrock_bridge", () -> bridge == null ? "None" : bridge.name()));
        metrics.addCustomChart(new SimplePie("server_flavor", () -> scheduler.isFolia() ? "Folia" : getServer().getName()));
        metrics.addCustomChart(new SimplePie("native_dialogs", () -> platforms.serverSupportsDialogs() && config.nativeDialogs() ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("chest_conversion", () -> config.convertChestGuis() ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new SimplePie("bedrock_players", () ->
                platforms.counts().getOrDefault(ClientPlatform.BEDROCK, 0) > 0 ? "Has Bedrock players" : "No Bedrock players"));
    }

    // ------------------------------------------------------------------ accessors

    public BridgeConfig config() {
        return config;
    }

    public PlatformScheduler scheduler() {
        return scheduler;
    }

    public PlatformResolver platforms() {
        return platforms;
    }

    public SessionManager sessions() {
        return sessions;
    }

    public TextSanitizer text() {
        return text;
    }

    public InventoryFilter filter() {
        return filter;
    }

    public ClickInjector injector() {
        return injector;
    }

    public BedrockFormRenderer bedrockRenderer() {
        return bedrockRenderer;
    }

    public JavaDialogRenderer dialogs() {
        return dialogs;
    }

    public LegacyChestRenderer legacyRenderer() {
        return legacyRenderer;
    }

    public ChatInputService chatInput() {
        return chatInput;
    }

    public ScheduledExecutorService executor() {
        return executor;
    }

    /** The active Bedrock bridge, or {@code null} when neither Floodgate nor Geyser is installed. */
    public BedrockBridge bridge() {
        return bridge;
    }
}
