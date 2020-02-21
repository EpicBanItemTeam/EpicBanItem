package com.github.euonmyoji.epicbanitem;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

@Singleton
public class ObservableFileServiceImpl implements ObservableFileService {
    private final Map<String, ObservableDirectory> observableDirectories;
    private final EventManager eventManager;
    private final PluginContainer pluginContainer;

    @Inject
    public ObservableFileServiceImpl(PluginContainer pluginContainer, EventManager eventManager) {
        this.pluginContainer = pluginContainer;
        this.eventManager = eventManager;

        this.observableDirectories = Maps.newHashMap();

        Task
            .builder()
            .async()
            .execute(task -> observableDirectories.values().forEach(observableDirectory -> observableDirectory.tick(task)))
            .intervalTicks(1)
            .submit(pluginContainer);
    }

    @Override
    public void register(ObservableFile observableFile) {
        Path filePath = observableFile.getPath().toAbsolutePath();
        Path dirPath = Files.isDirectory(filePath) ? filePath : filePath.getParent();
        String dirPathString = dirPath.toString();
        try {
            if (!observableDirectories.containsKey(dirPathString)) {
                observableDirectories.put(dirPathString, new ObservableDirectory(dirPath, pluginContainer, eventManager));
            }
            observableDirectories.get(dirPathString).register(observableFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
