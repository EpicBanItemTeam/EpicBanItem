package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.ObservableFile;
import com.github.euonmyoji.epicbanitem.Savable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Objects;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ObservableConfigFile implements ObservableFile, Savable {
    private final FileConsumer<ConfigurationNode> deleteConsumer;
    private final FileConsumer<ConfigurationNode> updateConsumer;
    private final FileConsumer<ConfigurationNode> saveConsumer;
    private ConfigurationNode node;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private final Path path;

    private ObservableConfigFile(Builder builder) throws IOException {
        this.deleteConsumer = builder.deleteConsumer;
        this.updateConsumer = builder.updateConsumer;
        this.saveConsumer = builder.saveConsumer;
        this.path = builder.path;

        this.configurationLoader = HoconConfigurationLoader.builder().setPath(Objects.requireNonNull(path)).build();

        this.next(StandardWatchEventKinds.ENTRY_MODIFY);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void next(WatchEvent.Kind<Path> kind) throws IOException {
        if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            this.deleteConsumer.accept(node);
        } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind) || StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
            this.node = configurationLoader.load();
            this.updateConsumer.accept(node);
        }
    }

    @Override
    public void save() throws IOException {
        this.saveConsumer.accept(node);
        this.configurationLoader.save(node);
    }

    @Override
    public Path getPath() {
        return path;
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
