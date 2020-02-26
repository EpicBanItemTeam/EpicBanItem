package team.ebi.epicbanitem.util.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Objects;

/**
 * @author The EpicBanItem Team
 */
public class ObservableDirectory implements ObservableFile, Closeable {
    private final Path path;
    private final FileConsumer<Path> createConsumer;
    private final FileConsumer<Path> deleteConsumer;
    private boolean closed;

    public ObservableDirectory(Builder builder) throws IOException {
        this.path = builder.path.toAbsolutePath();
        this.createConsumer = builder.createConsumer;
        this.deleteConsumer = builder.deleteConsumer;

        this.next(StandardWatchEventKinds.ENTRY_MODIFY, path);
    }

    @Override
    public void next(WatchEvent.Kind<Path> kind, Path path) throws IOException {
        if (closed) {
            return;
        }
        if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
            if (Objects.nonNull(this.deleteConsumer)) {
                this.deleteConsumer.accept(path);
            }
        } else if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
            if (Objects.nonNull(this.createConsumer)) {
                this.createConsumer.accept(path);
            }
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static final class Builder {
        private FileConsumer<Path> createConsumer;
        private FileConsumer<Path> deleteConsumer;
        private Path path;

        public Builder createConsumer(FileConsumer<Path> createConsumer) {
            this.createConsumer = createConsumer;
            return this;
        }

        public Builder deleteConsumer(FileConsumer<Path> deleteConsumer) {
            this.deleteConsumer = deleteConsumer;
            return this;
        }

        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        public ObservableDirectory build() throws IOException {
            return new ObservableDirectory(this);
        }
    }
}
