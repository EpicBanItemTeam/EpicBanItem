package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.listeners.GetItemListener;
import com.github.euonmyoji.epicbanitem.listeners.SummonListener;
import com.github.euonmyoji.epicbanitem.listeners.WorldItemMoveListener;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "epicbanitem", name = "EpicBanItem", version = EpicBanItem.VERSION, authors = {"yinyangshi", "GINYAI", "ustc-zzzz"})
public class EpicBanItem {
    static final String VERSION = "1.0";

    @Inject
    @ConfigDir(sharedRoot = false)
    public static Path cfgDir;

    @Inject
    public Logger logger;

    @Listener
    public void onStarting(GameStartingServerEvent event) {
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
        Sponge.getEventManager().registerListeners(new GetItemListener(), this);
        Sponge.getEventManager().registerListeners(new WorldItemMoveListener(), this);
        Sponge.getEventManager().registerListeners(new SummonListener(), this);
    }
}
