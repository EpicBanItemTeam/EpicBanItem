package team.ebi.epicbanitem.util.file;

import com.google.inject.ImplementedBy;

@ImplementedBy(ObservableFileServiceImpl.class)
public interface ObservableFileService {
    void register(ObservableFile observableFile);
}
