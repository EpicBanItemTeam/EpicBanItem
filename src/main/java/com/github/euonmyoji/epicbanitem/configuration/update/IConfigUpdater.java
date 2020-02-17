package com.github.euonmyoji.epicbanitem.configuration.update;

import java.io.IOException;
import java.nio.file.Path;

public interface IConfigUpdater {

    int getVersion();

    boolean canAccept(int version);

    void doUpdate(Path configDir, Path configPath) throws IOException;
}
