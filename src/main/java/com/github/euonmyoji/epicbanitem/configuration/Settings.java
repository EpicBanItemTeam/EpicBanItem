package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.check.Triggers;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

//应该在BanConfig之前

/**
 * @author epicbanitem authors
 */
public class Settings {
    private Path settingPath;
    private CommentedConfigurationNode cfg;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";

    public boolean listenLoadingChunk = false;
    private Set<String> enabledDefaultTriggers;
    private Set<String> disabledDefaultTriggers;

    public Settings(Path settingPath) {
        this.settingPath = settingPath;
        this.loader = HoconConfigurationLoader.builder().setPath(settingPath).build();
    }

    public void load() throws IOException {
        cfg = loader.load();

        listenLoadingChunk = cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).getBoolean(false);

        CommentedConfigurationNode defauldTriggers = cfg.getNode("epicbanitem", "default-trigger");
        Set<String> enabled = new LinkedHashSet<>(Triggers.getDefaultTriggers()), disabled = new LinkedHashSet<>();
        defauldTriggers.getChildrenMap().forEach((k, v) -> (v.getBoolean() ? enabled : disabled).add(k.toString()));

        enabledDefaultTriggers = Collections.unmodifiableSet(enabled);
        disabledDefaultTriggers = Collections.unmodifiableSet(disabled);
    }

    public void save() throws IOException {
        cfg.getNode("epicbanitem-version").setValue(1);
        cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).setValue(listenLoadingChunk);
        enabledDefaultTriggers.forEach(k -> cfg.getNode("epicbanitem", "default-trigger", k).setValue(true));
        disabledDefaultTriggers.forEach(k -> cfg.getNode("epicbanitem", "default-trigger", k).setValue(false));

        loader.save(cfg);
    }

    public Set<String> getEnabledDefaultTriggers() {
        return enabledDefaultTriggers;
    }

    public Set<String> getDisabledDefaultTriggers() {
        return disabledDefaultTriggers;
    }
}
