package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;

public class Settings {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private Settings() {
        //nothing here
    }

    public static void init(){
        loader = HoconConfigurationLoader.builder()
                .setPath(EpicBanItem.plugin.cfgDir.resolve("settings.conf")).build();
        reload();
    }

    public static void reload() {
        cfg = load();
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }

    public static boolean save() {
        try {
            loader.save(cfg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
