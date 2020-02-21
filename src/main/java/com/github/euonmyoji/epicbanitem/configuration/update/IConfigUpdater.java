package com.github.euonmyoji.epicbanitem.configuration.update;

import java.io.IOException;
import java.nio.file.Path;

public interface IConfigUpdater {
    int getTargetVersion();

    boolean canAccept(int version);

    void doUpdate(Path configPath) throws IOException;
}
