package com.github.euonmyoji.epicbanitem;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

public class ObservableDirectory implements ObservableFileService {
    private final Map<String, ObservableFile> registeredFiles;
    private final Map<String, Long> timestamps;
    private final WatchService watchService;
    private final Path directory;

    public ObservableDirectory(Path directory, PluginContainer pluginContainer, EventManager eventManager) throws IOException {
        this.registeredFiles = Maps.newHashMap();
        this.timestamps = Maps.newHashMap();
        this.watchService = FileSystems.getDefault().newWatchService();
        this.directory = directory;

        this.directory.register(this.watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Files.createDirectories(this.directory);

        eventManager.registerListener(pluginContainer, GameStoppingEvent.class, this::onStopping);
    }

    void tick(Task task) {
        WatchKey watchKey = this.watchService.poll();
        if (Objects.nonNull(watchKey)) {
            watchKey
                .pollEvents()
                .stream()
                .filter(watchEvent -> watchEvent.context() instanceof Path)
                .map(watchEvent -> (WatchEvent<Path>) watchEvent)
                .filter(
                    watchEvent -> {
                        // Filter duplicate event in Windows
                        Path path = directory.resolve(watchEvent.context());
                        String pathString = path.toString();
                        return !timestamps.containsKey(pathString) || Math.abs(timestamps.get(pathString) - path.toFile().lastModified()) > 50;
                    }
                )
                .forEach(
                    watchEvent -> {
                        Path path = watchEvent.context();
                        String pathString = path.toString();
                        if (registeredFiles.containsKey(pathString)) {
                            try {
                                registeredFiles.get(pathString).next(watchEvent.kind());
                                timestamps.put(pathString, directory.resolve(path).toFile().lastModified());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                );
            if (!watchKey.reset()) {
                task.cancel();
                // TODO: 2020/2/21 WHAT TO DO? Register?
            }
        }
    }

    @Override
    public void register(ObservableFile observableFile) {
        this.registeredFiles.put(observableFile.getPath().getFileName().toString(), observableFile);
    }

    private void onStopping(GameStoppingEvent event) {
        try {
            this.watchService.close();
            for (Savable savable : this.registeredFiles.values()
                .stream()
                .filter(Savable.class::isInstance)
                .map(Savable.class::cast)
                .collect(Collectors.toList())) {
                savable.save();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save EpicBanItem", e);
        }
    }
}
