package com.github.euonmyoji.epicbanitem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface ObservableFile {

    void next(WatchEvent.Kind<Path> kind) throws IOException;

    Path getPath();

    interface FileConsumer<T> {
        void accept(T t) throws IOException;
    }
}
