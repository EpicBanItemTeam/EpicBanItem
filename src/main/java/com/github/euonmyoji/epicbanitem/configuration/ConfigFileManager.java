package com.github.euonmyoji.epicbanitem.configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class ConfigFileManager {

    @Inject
    private Logger logger;

    private final Path configDir;
    private final PluginContainer pluginContainer;

    private final AutoFileLoader rootLoader;
    private final Map<String, AutoFileLoader> subDirLoaders = new LinkedHashMap<>();
    private final Task task;

    @Inject
    private ConfigFileManager(Logger logger, @ConfigDir(sharedRoot = false) Path configDir, PluginContainer pluginContainer) throws IOException {
        this.logger = logger;
        this.configDir = configDir.toAbsolutePath();
        this.pluginContainer = pluginContainer;
        this.rootLoader =  new AutoFileLoader(configDir, logger, pluginContainer, Sponge.getEventManager());
        task = Task.builder().execute(this::tick).name("EpicBanItemAutoFileLoader").intervalTicks(1).submit(pluginContainer);
    }

    private void tick(Task task) {
        rootLoader.tick(task);
        subDirLoaders.forEach((s, loader) -> loader.tick(task));
    }

    public AutoFileLoader getRootLoader() {
        return rootLoader;
    }

    public AutoFileLoader getOrCreateDirLoader(Path dir) throws IOException {
        Path absDir = dir.toAbsolutePath();
        if (absDir.startsWith(configDir)) {
            String pathString = configDir.relativize(absDir).toString();
            if (subDirLoaders.containsKey(pathString)) {
                return subDirLoaders.get(pathString);
            } else {
                AutoFileLoader loader = new AutoFileLoader(absDir, logger, pluginContainer, Sponge.getEventManager());
                subDirLoaders.put(pathString, loader);
                return loader;
            }
        } else {
            throw new IllegalArgumentException(dir + " is not under " + configDir);
        }
    }
}
