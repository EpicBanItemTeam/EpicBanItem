package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.util.Map;

//应该在BanConfig之前
//静态?
public class Settings {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private static final String LISTEN_CHUNK_LOAD = "Listen-chunk-load";

    public static boolean ListenLoadingChunk = false;

    private Settings() {
        //nothing here
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(EpicBanItem.plugin.cfgDir.resolve("settings.conf")).build();
        reload();
        cfg.getNode(LISTEN_CHUNK_LOAD).setValue(LISTEN_CHUNK_LOAD);
    }

    public static void reload() {
        cfg = load();
        ListenLoadingChunk = cfg.getNode(LISTEN_CHUNK_LOAD).getBoolean(false);
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

    public static Map<String,Boolean> getDefaultTriggers(){
        //todo:
        throw new UnsupportedOperationException("TODO :D");
    }

}
