package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.command.EpicBanItemCommand;
import com.github.euonmyoji.epicbanitem.configuration.BanItemConfig;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.listener.ChunkListener;
import com.github.euonmyoji.epicbanitem.listener.InventoryListener;
import com.github.euonmyoji.epicbanitem.listener.WorldItemMoveListener;
import com.github.euonmyoji.epicbanitem.message.Messages;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author EpicBanItem Team
 */
@Plugin(id = "epicbanitem", name = "EpicBanItem", version = EpicBanItem.VERSION, authors = {"yinyangshi", "GINYAI", "ustc-zzzz"},
        description = "a banitem plugin with nbt")
public class EpicBanItem {
    public static EpicBanItem plugin;
    static final String VERSION = "1.0";

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;

    public static Logger logger;

    private Messages messages;

    public Messages getMessages() {
        return messages;
    }

    @Inject
    public void setLogger(Logger logger) {
        EpicBanItem.logger = logger;
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        reload();
    }

    public static void reload() {
        logger.info("reloading");
        Settings.reload();
        BanItemConfig.reload();
        logger.info("reloaded");
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        plugin = this;
        logger.debug("Item to Block matching: ");
        NbtTagDataUtil.printLog().forEachRemaining(log -> logger.debug(log));
        if (!Files.exists(cfgDir)) {
            try {
                Files.createDirectory(cfgDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(CheckRule.class), new CheckRule.Serializer());
        Settings.init();
        BanItemConfig.init();
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, EpicBanItemCommand.ebi, "epicbanitem", "ebi", "banitem");
        Sponge.getEventManager().registerListeners(this, new InventoryListener());
        Sponge.getEventManager().registerListeners(this, new WorldItemMoveListener());
        Sponge.getEventManager().registerListeners(this, new ChunkListener());
    }
}
