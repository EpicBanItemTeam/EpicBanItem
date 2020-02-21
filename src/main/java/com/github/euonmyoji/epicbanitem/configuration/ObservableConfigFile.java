package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.util.file.ObservableFile;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.Objects;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ObservableConfigFile implements ObservableFile, Closeable {
    private final FileConsumer<ConfigurationNode> deleteConsumer;
    private final FileConsumer<ConfigurationNode> updateConsumer;
    private final FileConsumer<ConfigurationNode> saveConsumer;
    private ConfigurationNode node;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private final Path path;
    private boolean closed;

    private ObservableConfigFile(Builder builder) throws IOException {
        this.deleteConsumer = builder.deleteConsumer;
        this.updateConsumer = builder.updateConsumer;
        this.saveConsumer = builder.saveConsumer;
        this.path = builder.path.toAbsolutePath();

        this.configurationLoader = HoconConfigurationLoader.builder().setPath(Objects.requireNonNull(path)).build();

        this.next(StandardWatchEventKinds.ENTRY_MODIFY, path);
        this.save();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void save() throws IOException {
        if (closed) {
            return;
        }
        if (Objects.nonNull(this.saveConsumer)) {
            this.saveConsumer.accept(node);
            this.configurationLoader.save(node);
        }
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
            this.node = configurationLoader.load();
            if (Objects.nonNull(this.updateConsumer)) {
                this.updateConsumer.accept(node);
            }
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

        public ObservableConfigFile build() throws IOException {
            return new ObservableConfigFile(this);
        }
    }
}
