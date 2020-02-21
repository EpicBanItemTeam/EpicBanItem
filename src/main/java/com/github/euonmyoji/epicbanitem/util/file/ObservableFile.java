package com.github.euonmyoji.epicbanitem.util.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

public interface ObservableFile {

    Path getPath();

    void next(Kind<Path> kind, Path path) throws IOException;

    interface FileConsumer<T> {

        void accept(T t) throws IOException;
    }
}
