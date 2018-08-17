package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.CheckRuleServiceImpl;
import com.github.euonmyoji.epicbanitem.command.CommandEbi;
import com.github.euonmyoji.epicbanitem.configuration.AutoFileLoader;
import com.github.euonmyoji.epicbanitem.configuration.BanConfig;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.listener.ChunkListener;
import com.github.euonmyoji.epicbanitem.listener.InventoryListener;
import com.github.euonmyoji.epicbanitem.listener.WorldItemMoveListener;
import com.github.euonmyoji.epicbanitem.message.Messages;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author EpicBanItem Team
 */
@Plugin(id = "epicbanitem", name = "EpicBanItem", version = EpicBanItem.VERSION, authors = {"yinyangshi", "GiNYAi", "ustc-zzzz"},
        description = "a banitem plugin with nbt")
public class EpicBanItem {
    public static EpicBanItem plugin;
    public static final String VERSION = "0.1.0";

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;

    public static Logger logger;

    private Messages messages;

    public Messages getMessages() {
        return messages;
    }

    private Settings settings;

    public Settings getSettings() {
        return settings;
    }

    private BanConfig banConfig;

    public BanConfig getBanConfig() {
        return banConfig;
    }

    private String mainCommandAlias;

    public String getMainCommandAlias() {
        return mainCommandAlias;
    }

    private AutoFileLoader autoFileLoader;

    @Inject
    public void setLogger(Logger logger) {
        EpicBanItem.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        messages = new Messages(this, cfgDir);
        Sponge.getServiceManager().setProvider(this, CheckRuleService.class, new CheckRuleServiceImpl());
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        try {
            messages.load();
            autoFileLoader = new AutoFileLoader(this, cfgDir);
            settings = new Settings(autoFileLoader, cfgDir.resolve("settings.conf"));
            banConfig = new BanConfig(autoFileLoader, cfgDir.resolve("banitem.conf"));
        } catch (Exception e) {
            logger.error("Failed to load EpicBanItem", e);
        }
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        CommandEbi commandEbi = new CommandEbi();
        Optional<CommandMapping> commandMapping = Sponge.getCommandManager()
                .register(this, commandEbi.getCallable(), commandEbi.getNameList());
        if (!commandMapping.isPresent()) {
            //none registered
        } else {
            mainCommandAlias = commandMapping.get().getPrimaryAlias();
        }
        Sponge.getEventManager().registerListeners(this, new InventoryListener());
        Sponge.getEventManager().registerListeners(this, new WorldItemMoveListener());
        Sponge.getEventManager().registerListeners(this, new ChunkListener());
    }

    @Listener
    public void onStopping(GameStoppingServerEvent event) {
        try {
            autoFileLoader.close();
        } catch (Exception e) {
            logger.error("Failed to save EpicBanItem", e);
        }
    }
}
