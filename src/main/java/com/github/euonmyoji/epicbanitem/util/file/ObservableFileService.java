package com.github.euonmyoji.epicbanitem.util.file;

import com.google.inject.ImplementedBy;

@ImplementedBy(ObservableFileServiceImpl.class)
public interface ObservableFileService {
    void register(ObservableFile observableFile);
}
