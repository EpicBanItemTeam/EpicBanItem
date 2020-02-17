package com.github.euonmyoji.epicbanitem.configuration;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class AutoFileLoader implements Closeable {
    private final Path dirPath;
    private final WatchService service;

    private final Map<String, FileAction> updateListeners;
    private final Map<String, FileAction> deleteListeners;
    private final Map<String, FileAction> writeListeners;
    private final List<FileBiConsumer<Path, WatchEvent.Kind<?>>> fallbackListeners;

    private final Set<String> pendingLoadTasks;
    private final Set<String> pendingSaveTasks;

    private final Logger logger;

    public AutoFileLoader(Path dir, Logger logger, PluginContainer pluginContainer, EventManager eventManager) throws IOException {
        this.logger = logger;
        this.service = FileSystems.getDefault().newWatchService();
        this. dirPath = dir.toAbsolutePath();
        Files.createDirectories(dir);

        this.updateListeners = new LinkedHashMap<>();
        this.deleteListeners = new LinkedHashMap<>();
        this.writeListeners = new LinkedHashMap<>();
        this.fallbackListeners = new ArrayList<>();

        this.pendingLoadTasks = new LinkedHashSet<>();
        this.pendingSaveTasks = new LinkedHashSet<>();

        dir.register(this.service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        eventManager.registerListeners(pluginContainer, this);
    }

    private static ConfigurationLoader<? extends ConfigurationNode> toLoader(Path path) {
        return HoconConfigurationLoader.builder().setPath(path).build();
    }

    private String toString(Path path) {
        return dirPath.relativize(path).toString();
    }

    void tick(Task task) {
        this.tickPendingSaveTask();
        this.tickWatchEvents(task);
        this.tickPendingLoadTask();
    }

    private void tickWatchEvents(Task task) {
        WatchKey key = this.service.poll();
        if (Objects.nonNull(key)) {
            Map<String, WatchEvent.Kind<?>> pathStrings = new LinkedHashMap<>();
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() != OVERFLOW) {
                    Object path = event.context();
                    pathStrings.put(path.toString(), event.kind());
                }
            }
            pathStrings.forEach(
                (pathString, event) -> {
                    this.pendingLoadTasks.remove(pathString);
                    if (event != ENTRY_DELETE) {
                        if (this.updateListeners.containsKey(pathString)) {
                            logger.info(pathString + " has been changed. Try reloading now.");
                            try {
                                this.updateListeners.get(pathString).run();
                                logger.info(pathString + " reloaded successfully.");
                            } catch (IOException e) {
                                logger.error("Find error while reading from " + pathString + ".", e);
                            }
                        } else {
                            for (FileBiConsumer<Path, WatchEvent.Kind<?>> fallback: fallbackListeners) {
                                try {
                                    fallback.accept(dirPath.resolve(pathString), event);
                                } catch (IOException e) {
                                    logger.warn("Failed to handle file " + pathString + " change.", e);
                                }
                            }
                        }
                    } else {
                        if (this.deleteListeners.containsKey(pathString)) {
                            logger.info(pathString + " has been deleted. Try handling now.");
                            try {
                                this.deleteListeners.get(pathString).run();
                                logger.info(pathString + " reloaded successfully.");
                            } catch (IOException e) {
                                logger.error("Find error while reading from " + pathString + ".", e);
                            }
                        }
                    }
                }
            );
            if (!key.reset()) {
                task.cancel();
                //todo: add log & how to restart
            }
        }
    }

    private void tickPendingLoadTask() {
        if (!this.pendingLoadTasks.isEmpty()) {
            for (Iterator<String> i = this.pendingLoadTasks.iterator(); i.hasNext(); i.remove()) {
                String pathString = i.next();
                try {
                    this.updateListeners.get(pathString).run();
                    logger.info(pathString + " loaded successfully.");
                } catch (IOException e) {
                    logger.error("Find error while reading from " + pathString + ".", e);
                }
            }
        }
    }

    private void tickPendingSaveTask() {
        if (!this.pendingSaveTasks.isEmpty()) {
            for (Iterator<String> i = this.pendingSaveTasks.iterator(); i.hasNext(); i.remove()) {
                String pathString = i.next();
                try {
                    this.writeListeners.get(pathString).run();
                    logger.info(pathString + " saved successfully.");
                } catch (IOException e) {
                    logger.error("Find error writing reading from " + pathString + ".", e);
                }
            }
        }
    }

    public void forceSaving(Path path) {
        String pathString = toString(path);
        this.pendingSaveTasks.add(pathString);
    }


    public void addListener(Path path, FileAction updateListener, FileAction deleteListener, FileAction writeListener) {
        addListener(path, updateListener, deleteListener, writeListener, true);
    }

    public void addListener(Path path, FileAction updateListener, FileAction deleteListener, FileAction writeListener, boolean loading) {
        String pathString = toString(path);

        this.updateListeners.put(pathString, updateListener);
        this.deleteListeners.put(pathString, deleteListener);
        this.writeListeners.put(pathString, writeListener);

        if (loading) {
            this.pendingLoadTasks.add(pathString);
            this.tickPendingLoadTask();
        }
    }

    public boolean hasListener(Path path) {
        return writeListeners.containsKey(toString(path));
    }

    public void removeListener(Path path) {
        String pathString = toString(path);
        updateListeners.remove(pathString);
        deleteListeners.remove(pathString);
        writeListeners.remove(pathString);
    }

    public void addFallbackListener(FileBiConsumer<Path, WatchEvent.Kind<?>> fallbackListener) {
        this.fallbackListeners.add(fallbackListener);
    }

    @Override
    public void close() throws IOException {
        this.service.close();
        for (Map.Entry<String, FileAction> entry : this.writeListeners.entrySet()) {
            String pathString = entry.getKey();
            try {
                entry.getValue().run();
                logger.info(pathString + " saved successfully.");
            } catch (IOException e) {
                logger.error("Find error while writing to " + pathString + ".", e);
            }
        }
    }

    @FunctionalInterface
    public interface FileAction {
        void run() throws IOException;
    }

    @FunctionalInterface
    public interface FileBiConsumer<R, T> {
        void accept(R r, T t) throws IOException;
    }


    @Listener
    public void onStopping(GameStoppingEvent event) {
        try {
            this.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save EpicBanItem", e);
        }
    }
}
