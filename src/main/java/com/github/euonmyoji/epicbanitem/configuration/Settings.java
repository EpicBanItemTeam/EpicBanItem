package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

//应该在BanConfig之前

/**
 * @author epicbanitem authors
 */
public class Settings {
    private static Map<String,Boolean> triggers = new LinkedHashMap<>();
    static {
        triggers.put("use",true);
        triggers.put("throw",true);
        triggers.put("pickup",true);
        triggers.put("click",true);
    }

    private Path settingPath;
    private CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";

    public boolean listenLoadingChunk = false;
    private Map<String,Boolean> defaultTriggers;
    private Map<String,Boolean> immutableDefaultTriggers;

    public Settings(Path settingPath) {
        this.settingPath = settingPath;
        this.loader = HoconConfigurationLoader.builder().setPath(settingPath).build();
    }

    public void reload() throws IOException {
        Sponge.getAssetManager().getAsset(EpicBanItem.plugin, "default_settings.conf").get().copyToFile(settingPath, false);
        cfg = loader.load();
        CommentedConfigurationNode node = cfg.getNode("epicbanitem");
        listenLoadingChunk = node.getNode(LISTEN_CHUNK_LOAD).getBoolean(false);
        defaultTriggers = new LinkedHashMap<>();
        for(Map.Entry<String,Boolean> entry:triggers.entrySet()){
            defaultTriggers.put(entry.getKey(),node.getNode("default-trigger",entry.getKey()).getBoolean(entry.getValue()));
        }
        for(Map.Entry<?,? extends CommentedConfigurationNode> entry:node.getNode("default-trigger").getChildrenMap().entrySet()){
            if(!defaultTriggers.containsKey(entry.getKey().toString())){
                defaultTriggers.put(entry.getKey().toString(),entry.getValue().getBoolean());
            }
        }
        immutableDefaultTriggers = Collections.unmodifiableMap(defaultTriggers);
    }

    public Map<String, Boolean> getDefaultTriggers() {
        return immutableDefaultTriggers;
    }

}
