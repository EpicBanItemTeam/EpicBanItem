package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

//应该在BanConfig之前

/**
 * @author epicbanitem authors
 */
public class Settings {
    private Path settingPath;
    private CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    private static final String LISTEN_CHUNK_LOAD = "Listen-chunk-load";

    public boolean ListenLoadingChunk = false;

    public Settings(Path settingPath) {
        this.settingPath = settingPath;
        this.loader = HoconConfigurationLoader.builder().setPath(settingPath).build();
    }

    public void reload() throws IOException {
        Sponge.getAssetManager().getAsset(EpicBanItem.plugin, "default_settings.conf").get().copyToFile(settingPath, false);
        cfg = loader.load();
        ListenLoadingChunk = cfg.getNode(LISTEN_CHUNK_LOAD).getBoolean(false);
    }

    public Map<String, Boolean> getDefaultTriggers() {
        //todo:
        throw new UnsupportedOperationException("TODO :D");
    }

}
