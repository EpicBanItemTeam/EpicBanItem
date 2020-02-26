package team.ebi.epicbanitem.util.file;

import com.google.common.collect.Maps;
import org.spongepowered.api.scheduler.Task;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @author The EpicBanItem Team
 */
public class ObservableFileRegistry implements ObservableFileService, Closeable {
    private final Map<String, ObservableFile> registeredFiles;
    private final Map<String, Long> timestamps;
    private final WatchService watchService;
    private final Path directory;

    public ObservableFileRegistry(Path directory) throws IOException {
        this.registeredFiles = Maps.newHashMap();
        this.timestamps = Maps.newHashMap();
        this.watchService = FileSystems.getDefault().newWatchService();
        this.directory = directory;

        this.directory.register(this.watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Files.createDirectories(this.directory);
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
                        Path path = directory.resolve(watchEvent.context()).toAbsolutePath();
                        String pathString = path.toString();
                        return !timestamps.containsKey(pathString) || Math.abs(timestamps.get(pathString) - path.toFile().lastModified()) > 50;
                    }
                )
                .forEach(
                    watchEvent -> {
                        Path path = directory.resolve(watchEvent.context());
                        String pathString = path.toString();
                        try {
                            if (registeredFiles.containsKey(pathString)) {
                                registeredFiles.get(pathString).next(watchEvent.kind(), path);
                                //TODO: this is a temp fix for config save
                                //  the ConfigLoader will write to a temp file, then copy(overwrite) the target file
                                //  and I get 2 event of watching path on my windows.  delete & create
                                if (watchEvent.kind() != ENTRY_DELETE) {
                                    timestamps.put(pathString, path.toFile().lastModified());
                                }
                            }
                            String parentPathString = path.getParent().toString();
                            if (registeredFiles.containsKey(parentPathString)) {
                                registeredFiles.get(parentPathString).next(watchEvent.kind(), path);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
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
        this.registeredFiles.put(observableFile.getPath().toString(), observableFile);
    }

    @Override
    public void close() throws IOException {
        this.watchService.close();
        for (ObservableFile observableFile : this.registeredFiles.values()) {
            observableFile.close();
        }
    }
}
