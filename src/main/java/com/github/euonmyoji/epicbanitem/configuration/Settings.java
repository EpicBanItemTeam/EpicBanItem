package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class Settings {

    private static final String DEFAULT_WORLD = "default-world";
    private static final String DEFAULT_TRIGGER = "default-trigger";
    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";

    private final Server server = Sponge.getServer();

    private boolean listenLoadingChunk = false;
    private Map<String, Boolean> enabledWorlds = Maps.newLinkedHashMap();
    private Map<String, Boolean> enabledTriggers = Maps.newLinkedHashMap();

    public Settings(AutoFileLoader fileLoader, Path settingPath) {
        this.resetToDefault();
        fileLoader.addListener(settingPath, this::load, this::save);
        if (Files.notExists(settingPath)) {
            fileLoader.forceSaving(settingPath);
        }
    }

    private void resetToDefault() {
        this.listenLoadingChunk = false;
        Collection<WorldProperties> worlds = this.server.getAllWorldProperties();
        this.enabledTriggers = Maps.newLinkedHashMap(Maps.toMap(Triggers.getDefaultTriggers(), k -> true));
        this.enabledWorlds = Maps.newLinkedHashMap(Maps.toMap(Iterables.transform(worlds, WorldProperties::getWorldName), k -> true));
    }

    private void load(ConfigurationNode cfg) {
        this.resetToDefault();
        ConfigurationNode defaultWorlds = cfg.getNode("epicbanitem", DEFAULT_WORLD);
        ConfigurationNode defaultTriggers = cfg.getNode("epicbanitem", DEFAULT_TRIGGER);
        this.listenLoadingChunk = cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).getBoolean(false);
        defaultWorlds.getChildrenMap().forEach((k, v) -> this.enabledWorlds.put(k.toString(), v.getBoolean()));
        defaultTriggers.getChildrenMap().forEach((k, v) -> this.enabledTriggers.put(k.toString(), v.getBoolean()));
    }

    private void save(ConfigurationNode cfg) {
        cfg.getNode("epicbanitem-version").setValue(BanConfig.CURRENT_VERSION);
        cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).setValue(this.listenLoadingChunk);
        this.enabledWorlds.forEach((k, v) -> cfg.getNode("epicbanitem", DEFAULT_WORLD, k).setValue(v));
        this.enabledTriggers.forEach((k, v) -> cfg.getNode("epicbanitem", DEFAULT_TRIGGER, k).setValue(v));
    }

    public boolean isListeningLoadingChunk() {
        return this.listenLoadingChunk;
    }

    public boolean isWorldDefaultEnabled(String world) {
        return this.enabledWorlds.getOrDefault(world, true);
    }

    public boolean isTriggerDefaultEnabled(String trigger) {
        return this.enabledTriggers.getOrDefault(trigger, true);
    }
}
