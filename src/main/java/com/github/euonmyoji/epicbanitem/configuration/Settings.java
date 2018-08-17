package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.check.Triggers;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

//应该在BanConfig之前

/**
 * @author epicbanitem authors
 */
@NonnullByDefault
public class Settings {

    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";

    private boolean listenLoadingChunk = false;
    private Set<String> enabledDefaultTriggers = Collections.emptySet();
    private Set<String> disabledDefaultTriggers = Collections.emptySet();

    public Settings(AutoFileLoader fileLoader, Path settingPath) {
        fileLoader.addListener(settingPath, this::load, this::save);
    }

    private void load(ConfigurationNode cfg) throws IOException {
        listenLoadingChunk = cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).getBoolean(false);

        ConfigurationNode defauldTriggers = cfg.getNode("epicbanitem", "default-trigger");
        Set<String> enabled = new LinkedHashSet<>(Triggers.getDefaultTriggers()), disabled = new LinkedHashSet<>();
        defauldTriggers.getChildrenMap().forEach((k, v) -> (v.getBoolean() ? enabled : disabled).add(k.toString()));

        enabledDefaultTriggers = Collections.unmodifiableSet(enabled);
        disabledDefaultTriggers = Collections.unmodifiableSet(disabled);
    }

    private void save(ConfigurationNode cfg) throws IOException {
        cfg.getNode("epicbanitem-version").setValue(BanConfig.CURRENT_VERSION);
        cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).setValue(listenLoadingChunk);
        enabledDefaultTriggers.forEach(k -> cfg.getNode("epicbanitem", "default-trigger", k).setValue(true));
        disabledDefaultTriggers.forEach(k -> cfg.getNode("epicbanitem", "default-trigger", k).setValue(false));
    }

    public boolean isListeningLoadingChunk() {
        return listenLoadingChunk;
    }

    public Set<String> getEnabledDefaultTriggers() {
        return enabledDefaultTriggers;
    }

    public Set<String> getDisabledDefaultTriggers() {
        return disabledDefaultTriggers;
    }
}
