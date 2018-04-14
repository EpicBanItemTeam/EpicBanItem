package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.command.EpicBanItemCommand;
import com.github.euonmyoji.epicbanitem.listener.GetItemListener;
import com.github.euonmyoji.epicbanitem.listener.SummonListener;
import com.github.euonmyoji.epicbanitem.listener.WorldItemMoveListener;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author 主yinyangshi
 */
@Plugin(id = "epicbanitem", name = "EpicBanItem", version = EpicBanItem.VERSION, authors = {"yinyangshi", "GINYAI", "ustc-zzzz"})
public class EpicBanItem {
    public static EpicBanItem plugin;
    static final String VERSION = "1.0";

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;

    @Inject
    public Logger logger;

    @Listener
    public void onReload(GameReloadEvent event) {
        reload();
    }

    public static void reload() {
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        plugin = this;
        if (!Files.exists(cfgDir)) {
            try {
                Files.createDirectory(cfgDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, EpicBanItemCommand.ebi, "epicbanitem", "ebi", "banitem");
        Sponge.getEventManager().registerListeners(this, new GetItemListener());
        Sponge.getEventManager().registerListeners(this, new WorldItemMoveListener());
        Sponge.getEventManager().registerListeners(this, new SummonListener());
    }
}
