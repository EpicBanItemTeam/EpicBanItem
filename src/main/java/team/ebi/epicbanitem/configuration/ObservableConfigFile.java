package team.ebi.epicbanitem.configuration;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.file.ObservableFile;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ObservableConfigFile implements ObservableFile, Closeable {
    private final FileConsumer<ConfigurationNode> deleteConsumer;
    private final FileConsumer<ConfigurationNode> updateConsumer;
    private final FileConsumer<ConfigurationNode> saveConsumer;
    private ConfigurationNode node;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private final Path path;
    private final Path configDir;
    private boolean closed;

    private ObservableConfigFile(Builder builder) throws IOException {
        this.deleteConsumer = builder.deleteConsumer;
        this.updateConsumer = builder.updateConsumer;
        this.saveConsumer = builder.saveConsumer;
        this.path = builder.path.toAbsolutePath();
        this.configDir = builder.configDir;

        this.configurationLoader = HoconConfigurationLoader.builder().setPath(Objects.requireNonNull(path)).build();

    }

    public static Builder builder() {
        return new Builder();
    }

    public void load() throws IOException {
        if (closed) {
            return;
        }
        Path backup = backup();
        try {
            this.node = configurationLoader.load();
            if (Objects.nonNull(this.updateConsumer)) {
                this.updateConsumer.accept(node);
            }
        } catch (Exception e) {
            if (backup != null) {
                EpicBanItem.getLogger().warn("Failed to load config file {}, a backup is created at {}", path, backup);
            }
            throw e;
        }
        if (backup != null) {
            Files.delete(backup);
        }
    }

    public void save() throws IOException {
        if (closed) {
            return;
        }
        if (Objects.nonNull(this.saveConsumer)) {
            this.node = configurationLoader.createEmptyNode();
            this.saveConsumer.accept(node);
            this.configurationLoader.save(node);
        }
    }

    @Nullable
    public Path backup() throws IOException {
        if (!Files.exists(this.path)) {
            return null;
        }
        Path backupDir = configDir.resolve("backup");
        Path aPath = backupDir.resolve(configDir.relativize(this.path));
        Path dir = aPath.getParent();
        Files.createDirectories(dir);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmm_"));
        String fileName = timestamp + aPath.getFileName();
        int i = 0;
        do {
            Path backupPath = dir.resolve(fileName);
            if (!Files.exists(backupPath)) {
                Files.copy(this.path, backupPath);
                return backupPath;
            }
            fileName = timestamp + getPath().getFileName() + "_" + i++;
        } while (i < 10);
        throw new IOException("cannot create backup for " + this.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObservableConfigFile that = (ObservableConfigFile) o;
        return (
            Objects.equals(deleteConsumer, that.deleteConsumer) &&
            Objects.equals(updateConsumer, that.updateConsumer) &&
            Objects.equals(saveConsumer, that.saveConsumer) &&
            path.equals(that.path)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(deleteConsumer, updateConsumer, saveConsumer, path);
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void next(Kind<Path> kind, Path path) throws IOException {
        if (closed) {
            return;
        }
        if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            if (Objects.nonNull(this.deleteConsumer)) {
                this.deleteConsumer.accept(node);
            }
        } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind) || StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
            load();
        }
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    public static final class Builder {
        private FileConsumer<ConfigurationNode> deleteConsumer;
        private FileConsumer<ConfigurationNode> updateConsumer;
        private FileConsumer<ConfigurationNode> saveConsumer;
        private Path path;
        private Path configDir;

        public Builder deleteConsumer(final FileConsumer<ConfigurationNode> deleteConsumer) {
            this.deleteConsumer = deleteConsumer;
            return this;
        }

        public Builder updateConsumer(final FileConsumer<ConfigurationNode> updateConsumer) {
            this.updateConsumer = updateConsumer;
            return this;
        }

        /**
         * Null for read-only
         */
        public Builder saveConsumer(final FileConsumer<ConfigurationNode> saveConsumer) {
            this.saveConsumer = saveConsumer;
            return this;
        }

        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        //for backup
        public Builder configDir(Path configDir) {
            this.configDir = configDir;
            return this;
        }

        public ObservableConfigFile build() throws IOException {
            return new ObservableConfigFile(this);
        }
    }
}
