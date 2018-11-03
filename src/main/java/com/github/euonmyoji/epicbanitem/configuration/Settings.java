package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

//应该在BanConfig之前

/**
 * @author epicbanitem authors
 */
@NonnullByDefault
public class Settings {

    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";

    private boolean listenLoadingChunk = false;
    private Map<String, Boolean> enabledDefaultTriggers;
    private Map<String, Boolean> enabledDefaultWorlds;

    public Settings(AutoFileLoader fileLoader, Path settingPath) {
        Collection<WorldProperties> worlds = Sponge.getServer().getAllWorldProperties();
        this.enabledDefaultTriggers = Maps.toMap(Triggers.getDefaultTriggers(), k -> true);
        this.enabledDefaultWorlds = Maps.toMap(Iterables.transform(worlds, WorldProperties::getWorldName), k -> true);

        fileLoader.addListener(settingPath, this::load, this::save);
        if (Files.notExists(settingPath)) {
            fileLoader.forceSaving(settingPath);
        }
    }

    private void load(ConfigurationNode cfg) {
        listenLoadingChunk = cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).getBoolean(false);
        ConfigurationNode defaultTriggers = cfg.getNode("epicbanitem", "default-trigger");
        Map<String, Boolean> enabledTrigger = new LinkedHashMap<>();
        defaultTriggers.getChildrenMap().forEach((k, v) -> enabledTrigger.put(k.toString(), v.getBoolean()));
        enabledDefaultTriggers = Collections.unmodifiableMap(enabledTrigger);
        ConfigurationNode defaultWorlds = cfg.getNode("epicbanitem", "default-world");
        Map<String, Boolean> enabledWorld = new LinkedHashMap<>();
        defaultWorlds.getChildrenMap().forEach((k, v) -> enabledWorld.put(k.toString(), v.getBoolean()));
        enabledDefaultWorlds = Collections.unmodifiableMap(enabledWorld);
    }

    private void save(ConfigurationNode cfg) {
        cfg.getNode("epicbanitem-version").setValue(BanConfig.CURRENT_VERSION);
        cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).setValue(listenLoadingChunk);
        enabledDefaultTriggers.forEach((k, v) -> cfg.getNode("epicbanitem", "default-trigger", k).setValue(v));
        enabledDefaultWorlds.forEach((k, v) -> cfg.getNode("epicbanitem", "default-world", k).setValue(v));
    }

    public boolean isListeningLoadingChunk() {
        return listenLoadingChunk;
    }

    public Map<String, Boolean> getEnabledDefaultTriggers() {
        return enabledDefaultTriggers;
    }

    public Map<String, Boolean> getEnabledDefaultWorlds() {
        return enabledDefaultWorlds;
    }

    public boolean isTriggerDefaultEnabled(String trigger) {
        return enabledDefaultTriggers.getOrDefault(trigger, true);
    }

    public boolean isWorldDefaultEnabled(String world) {
        return enabledDefaultWorlds.getOrDefault(world, true);
    }
}
