package team.ebi.epicbanitem.check;

import com.google.inject.Inject;
import java.util.Objects;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;
import team.ebi.epicbanitem.handler.ChunkListener;
import team.ebi.epicbanitem.handler.DropHandler;
import team.ebi.epicbanitem.handler.InventoryListener;
import team.ebi.epicbanitem.handler.ThrowHandler;
import team.ebi.epicbanitem.handler.WorldListener;

public class CheckModule {

    private PluginContainer pluginContainer;

    @Inject
    private CheckRuleService checkRuleService;

    @Inject
    public CheckModule(
        ChunkListener chunkListener,
        InventoryListener inventoryListener,
        WorldListener worldListener,
        ThrowHandler throwHandler,
        DropHandler dropHandler,
        Triggers triggers,
        PluginContainer pluginContainer,
        EventManager eventManager
    ) {
        this.pluginContainer = pluginContainer;

        Objects.requireNonNull(chunkListener);
        Objects.requireNonNull(inventoryListener);
        Objects.requireNonNull(worldListener);
        Objects.requireNonNull(throwHandler);
        Objects.requireNonNull(dropHandler);

        Objects.requireNonNull(triggers);

        eventManager.registerListener(pluginContainer, GamePreInitializationEvent.class, this::onPreInit);
    }

    private void onPreInit(GamePreInitializationEvent event) {
        ServiceManager serviceManager = Sponge.getServiceManager();
        serviceManager.setProvider(pluginContainer, team.ebi.epicbanitem.api.CheckRuleService.class, checkRuleService);
        serviceManager.setProvider(pluginContainer, CheckRuleService.class, checkRuleService);
    }
}
