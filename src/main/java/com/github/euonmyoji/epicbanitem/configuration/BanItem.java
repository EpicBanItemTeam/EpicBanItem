package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.item.ItemType;

import java.io.IOException;
import java.util.List;

public class BanItem {
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private BanItem() {
        //nothing here
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(EpicBanItem.plugin.cfgDir.resolve("banitem.conf")).build();
        cfg.getNode("config-version").setValue(cfg.getNode("config-version").getInt(1));
        reload();
    }

    public static void reload() {
        cfg = load();
    }

    public static List<ItemType> bannedItemType() {
        cfg.getNode("epicbanitem").getChildrenList();
        //TODO
        return null;
    }

    public static CommentedConfigurationNode getCfg() {
        return cfg;
    }

    private static CommentedConfigurationNode load() {
        try {
            return loader.load();
        } catch (IOException e) {
            return loader.createEmptyNode(ConfigurationOptions.defaults());
        }
    }
}
