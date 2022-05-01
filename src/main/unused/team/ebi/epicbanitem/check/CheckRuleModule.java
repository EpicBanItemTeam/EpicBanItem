package team.ebi.epicbanitem.check;

import com.google.inject.Inject;
import java.util.Objects;
import team.ebi.epicbanitem.handler.ChunkListener;
import team.ebi.epicbanitem.handler.DropHandler;
import team.ebi.epicbanitem.handler.InventoryListener;
import team.ebi.epicbanitem.handler.ThrowHandler;
import team.ebi.epicbanitem.handler.WorldListener;

public class CheckRuleModule {

    @Inject
    public CheckRuleModule(
        ChunkListener chunkListener,
        InventoryListener inventoryListener,
        WorldListener worldListener,
        ThrowHandler throwHandler,
        DropHandler dropHandler,
        Triggers triggers
    ) {
        Objects.requireNonNull(chunkListener);
        Objects.requireNonNull(inventoryListener);
        Objects.requireNonNull(worldListener);
        Objects.requireNonNull(throwHandler);
        Objects.requireNonNull(dropHandler);

        Objects.requireNonNull(triggers);
    }
}
