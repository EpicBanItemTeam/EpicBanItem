package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.CheckRuleServiceImpl;
import com.github.euonmyoji.epicbanitem.check.listener.ChunkListener;
import com.github.euonmyoji.epicbanitem.check.listener.InventoryListener;
import com.github.euonmyoji.epicbanitem.check.listener.WorldItemMoveListener;
import com.github.euonmyoji.epicbanitem.command.CommandEbi;
import com.github.euonmyoji.epicbanitem.configuration.AutoFileLoader;
import com.github.euonmyoji.epicbanitem.configuration.BanConfig;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.message.Messages;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@Plugin(id = "epicbanitem", name = "EpicBanItem", version = "@version@",
        dependencies = @Dependency(id = Platform.API_ID, version = "7.1.0"),
        authors = {"yinyangshi", "GiNYAi", "ustc-zzzz"}, description = "A banitem with nbt plugin in sponge")
public class EpicBanItem {
    private static EpicBanItem instance;

    private final Path cfgDir;
    private final Logger logger;
    private final Metrics metrics;
    private final Messages messages;

    private Settings settings;
    private BanConfig banConfig;
    private String mainCommandAlias;
    private AutoFileLoader autoFileLoader;

    @Inject
    public EpicBanItem(@ConfigDir(sharedRoot = false) Path theCfgDir, Logger theLogger, Metrics theMetrics) {
        instance = this;
        cfgDir = theCfgDir;
        logger = theLogger;
        metrics = theMetrics;
        messages = new Messages(this, theCfgDir);
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static Messages getMessages() {
        return instance.messages;
    }

    public static Settings getSettings() {
        return instance.settings;
    }

    public static BanConfig getBanConfig() {
        return instance.banConfig;
    }

    public static String getMainCommandAlias() {
        return instance.mainCommandAlias;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        Sponge.getServiceManager().setProvider(this, CheckRuleService.class, new CheckRuleServiceImpl());
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        try {
            messages.load();
            autoFileLoader = new AutoFileLoader(this, cfgDir);
            settings = new Settings(autoFileLoader, cfgDir.resolve("settings.conf"));
            banConfig = new BanConfig(autoFileLoader, cfgDir.resolve("banitem.conf"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load EpicBanItem", e);
        }
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        Optional<CommandMapping> mappingOptional = new CommandEbi().registerFor(this);
        mappingOptional.ifPresent(mapping -> mainCommandAlias = mapping.getPrimaryAlias());
        metrics.addCustomChart(new Metrics.SingleLineChart("enabledCheckRules", () -> banConfig.getRules().size()));
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        EventManager eventManager = Sponge.getEventManager();
        eventManager.registerListeners(this, new ChunkListener());
        eventManager.registerListeners(this, new InventoryListener());
        eventManager.registerListeners(this, new WorldItemMoveListener());
        NbtTagDataUtil.printToLogger(logger::debug, settings.printItemToBlockMapping());
        logger.debug("Change the value of 'print-item-to-block-mapping' to enable or disable detailed output.");
    }

    @Listener
    public void onStopping(GameStoppingEvent event) {
        try {
            if (autoFileLoader != null) {
                autoFileLoader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save EpicBanItem", e);
        }
    }
}
