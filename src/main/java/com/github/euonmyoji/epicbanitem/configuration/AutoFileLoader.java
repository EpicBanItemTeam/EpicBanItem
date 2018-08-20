package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.scheduler.Task;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author ustc_zzzz
 */
public class AutoFileLoader implements Closeable {
    private final Path cfgDir;
    private final WatchService service;
    private final Set<String> pendingPathStringSaveTasks;
    private final Set<String> pendingPathStringLoadTasks;
    private final Map<String, FileConsumer> readListeners;
    private final Map<String, FileConsumer> writeListeners;
    private final Map<String, ConfigurationLoader<? extends ConfigurationNode>> configurationLoaders;

    public AutoFileLoader(EpicBanItem plugin, Path configDir) throws IOException {
        this.cfgDir = configDir.toAbsolutePath();
        this.readListeners = new LinkedHashMap<>();
        this.writeListeners = new LinkedHashMap<>();
        this.configurationLoaders = new LinkedHashMap<>();
        this.pendingPathStringSaveTasks = new LinkedHashSet<>();
        this.pendingPathStringLoadTasks = new LinkedHashSet<>();
        this.service = FileSystems.getDefault().newWatchService();
        configDir.register(this.service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Task.builder().execute(this::tick).name("EpicBanItemAutoFileLoader").intervalTicks(1).submit(plugin);
    }

    private void tick(Task task) {
        this.tickPendingSaveTask();
        this.tickWatchEvents(task);
        this.tickPendingLoadTask();
    }

    private void tickWatchEvents(Task task) {
        WatchKey key = this.service.poll();
        if (Objects.nonNull(key)) {
            Set<String> pathStrings = new LinkedHashSet<>();
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() != OVERFLOW) {
                    Object path = event.context();
                    pathStrings.add(path.toString());
                }
            }
            for (String pathString : pathStrings) {
                this.pendingPathStringLoadTasks.remove(pathString);
                if (this.configurationLoaders.containsKey(pathString)) {
                    EpicBanItem.logger.info(pathString + " has been changed. Try reloading now.");
                    this.loadFile(pathString, pathString + " reloaded successfully.");
                }
            }
            if (!key.reset()) {
                task.cancel();
            }
        }
    }

    private void tickPendingLoadTask() {
        if (!this.pendingPathStringLoadTasks.isEmpty()) {
            for (Iterator<String> i = this.pendingPathStringLoadTasks.iterator(); i.hasNext(); i.remove()) {
                String pathString = i.next();
                this.loadFile(pathString, pathString + " loaded successfully.");
            }
        }
    }

    private void tickPendingSaveTask() {
        if (!this.pendingPathStringSaveTasks.isEmpty()) {
            for (Iterator<String> i = this.pendingPathStringSaveTasks.iterator(); i.hasNext(); i.remove()) {
                String pathString = i.next();
                this.saveFile(pathString, pathString + " saved successfully.");
            }
        }
    }

    private void loadFile(String pathString, String msg) {
        try {
            ConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoaders.get(pathString);
            this.readListeners.get(pathString).accept(loader.load());
            EpicBanItem.logger.info(msg);
        } catch (IOException e) {
            EpicBanItem.logger.error("Find error while reading from " + pathString + ".", e);
        }
    }

    private void saveFile(String pathString, String msg) {
        try {
            ConfigurationLoader<? extends ConfigurationNode> loader = this.configurationLoaders.get(pathString);
            ConfigurationNode configurationNode = loader.createEmptyNode();
            this.writeListeners.get(pathString).accept(configurationNode);
            loader.save(configurationNode);
            EpicBanItem.logger.info(msg);
        } catch (IOException e) {
            EpicBanItem.logger.error("Find error while writing to " + pathString + ".", e);
        }
    }

    private static ConfigurationLoader<? extends ConfigurationNode> toLoader(Path path) {
        return HoconConfigurationLoader.builder().setPath(path).build();
    }

    private static String toString(Path path, Path cfgDir) {
        return cfgDir.relativize(path).toString();
    }

    public void forceSaving(Path path) {
        String pathString = toString(path, this.cfgDir);
        this.pendingPathStringSaveTasks.add(pathString);
    }

    public void addListener(Path path, FileConsumer readListener, FileConsumer writeListener) {
        String pathString = toString(path, this.cfgDir);
        this.pendingPathStringLoadTasks.add(pathString);
        this.readListeners.put(pathString, readListener);
        this.writeListeners.put(pathString, writeListener);
        this.configurationLoaders.put(pathString, toLoader(path));
    }

    @Override
    public void close() throws IOException {
        this.service.close();
        for (String pathString : this.configurationLoaders.keySet()) {
            this.saveFile(pathString, pathString + " saved successfully.");
        }
    }

    @FunctionalInterface
    public interface FileConsumer {
        void accept(ConfigurationNode node) throws IOException;
    }
}
