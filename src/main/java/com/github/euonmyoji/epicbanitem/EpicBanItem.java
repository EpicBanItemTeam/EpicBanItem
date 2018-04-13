package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.listeners.GetItemListener;
import com.github.euonmyoji.epicbanitem.listeners.WorldItemMoveListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "epicbanitem", name = "EpicBanItem", version = "1.0", authors = {"yinyangshi", "GINYAI", "ustc-zzzz"})
public class EpicBanItem {

    @Inject
    @ConfigDir(sharedRoot = false)
    public static Path cfgDir;

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
    }
}
