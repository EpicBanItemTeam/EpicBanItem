package team.ebi.epicbanitem.configuration;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.AffectItemStackEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckRuleTrigger;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.UpdateChecker;
import team.ebi.epicbanitem.util.file.ObservableFileService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
// TODO: 2020/2/21 ConfigSerializable
public class Settings {
    private static final int CURRENT_VERSION = 1;

    private static final String DEFAULT_WORLD = "default-world";
    private static final String DEFAULT_TRIGGER = "default-trigger";
    private static final String PRINT_ITEM_TO_BLOCK_MAPPING = "print-item-to-block-mapping";
    //    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";  not impl yet

    private Server server = Sponge.getServer();

    @Nullable
    private final Class<? extends AffectItemStackEvent> eventClass;

    private boolean listenLoadingChunk = false;
    private boolean printItemToBlockMapping = true;
    private boolean checkUpdate = true;

    private Map<String, Boolean> enabledWorlds = Maps.newLinkedHashMap();
    private Map<CheckRuleTrigger, Boolean> enabledTriggers = Maps.newLinkedHashMap();

    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private ObservableFileService fileService;

    @Inject
    private Injector injector;

    @Inject
    private Settings(EventManager eventManager, PluginContainer pluginContainer, Logger logger) {
        this.logger = logger;
        eventClass = getClassForCraftingResultRedirectionEvent();
        eventManager.registerListener(pluginContainer, GamePostInitializationEvent.class, this::onPostInit);
        eventManager.registerListener(pluginContainer, GameStartedServerEvent.class, this::onStarted);
    }

    @Nullable
    private Class<? extends AffectItemStackEvent> getClassForCraftingResultRedirectionEvent() {
        try {
            // noinspection SpellCheckingInspection
            String prefix = "com.github.ustc_zzzz.craftingreciperedirector.api";
            return Class.forName(prefix + ".CraftingResultRedirectionEvent").asSubclass(AffectItemStackEvent.class);
        } catch (Exception e) {
            PluginManager manager = Sponge.getPluginManager();
            if (manager.getPlugin("FML").isPresent() || manager.getPlugin("fml").isPresent()) {
                // noinspection SpellCheckingInspection
                logger.warn("A mod named CraftingResultRedirector is not available on your modded server.");
                logger.warn("We recommend installing the mod for filtering and modifying the crafting results.");
                logger.warn("It can be downloaded at: https://github.com/ustc-zzzz/CraftingRecipeRedirector/releases");
            }
        }
        return null;
    }

    private void resetToDefault() {
        this.listenLoadingChunk = false;
        this.printItemToBlockMapping = true;
        Collection<WorldProperties> worlds = this.server.getAllWorldProperties();
        this.enabledTriggers = Maps.newLinkedHashMap(Maps.toMap(Triggers.getTriggers().values(), k -> true));
        this.enabledWorlds = Maps.newLinkedHashMap(Maps.toMap(Iterables.transform(worlds, WorldProperties::getWorldName), k -> true));
    }

    private void load(ConfigurationNode node) throws IOException {
        this.resetToDefault();

        this.printItemToBlockMapping = node.getNode("epicbanitem", PRINT_ITEM_TO_BLOCK_MAPPING).getBoolean(true);
        this.checkUpdate = node.getNode("epicbantiem", "check-update").getBoolean(true);

        ConfigurationNode defaultWorlds = node.getNode("epicbanitem", DEFAULT_WORLD);
        defaultWorlds.getChildrenMap().forEach((k, v) -> this.enabledWorlds.put(k.toString(), v.getBoolean()));

        ConfigurationNode defaultTriggers = node.getNode("epicbanitem", DEFAULT_TRIGGER);
        defaultTriggers
            .getChildrenMap()
            .forEach(
                (k, v) -> {
                    String key = k.toString();
                    Optional<CheckRuleTrigger> optionalTrigger;
                    if (key.indexOf(':') == -1) {
                        optionalTrigger = Sponge.getRegistry().getType(CheckRuleTrigger.class, EpicBanItem.PLUGIN_ID + ":" + key);
                    } else {
                        optionalTrigger = Sponge.getRegistry().getType(CheckRuleTrigger.class, key);
                    }
                    if (!optionalTrigger.isPresent()) {
                        logger.warn("Find unknown trigger {} at global default settings, it will be ignored.", key);
                    } else {
                        enabledTriggers.put(optionalTrigger.get(), v.getBoolean());
                    }
                }
            );
    }

    private void save(ConfigurationNode node) throws IOException {
        node.getNode("epicbanitem-version").setValue(CURRENT_VERSION);

        node.getNode("epicbanitem", PRINT_ITEM_TO_BLOCK_MAPPING).setValue(this.printItemToBlockMapping);
        node.getNode("epicbanitem", "check-update").setValue(this.checkUpdate);

        this.enabledWorlds.forEach((k, v) -> node.getNode("epicbanitem", DEFAULT_WORLD, k).setValue(v));

        this.enabledTriggers.forEach((k, v) -> node.getNode("epicbanitem", DEFAULT_TRIGGER, k).setValue(v));
    }

    public boolean printItemToBlockMapping() {
        return this.printItemToBlockMapping;
    }

    @SuppressWarnings("unused because the code is not done")
    public boolean isListeningLoadingChunk() {
        return this.listenLoadingChunk;
    }

    public boolean isWorldDefaultEnabled(String world) {
        return this.enabledWorlds.getOrDefault(world, true);
    }

    public boolean isTriggerDefaultEnabled(CheckRuleTrigger trigger) {
        return this.enabledTriggers.getOrDefault(trigger, true);
    }

    public boolean isCraftingEventClass(AffectItemStackEvent event) {
        return this.eventClass == null ? event instanceof CraftItemEvent.Preview : this.eventClass.isInstance(event);
    }

    private void onPostInit(GamePostInitializationEvent event) throws IOException {
        ObservableConfigFile configFile = ObservableConfigFile.builder().path(configDir.resolve("settings.conf")).configDir(configDir).saveConsumer(this::save).updateConsumer(this::load).build();
        fileService.register(configFile);
        configFile.load();
        configFile.save();
    }

    private void onStarted(GameStartedServerEvent event) {
        NbtTagDataUtil.printToLogger(logger::debug, this.printItemToBlockMapping());
        logger.debug("Change the value of 'print-item-to-block-mapping' to enable or disable detailed output.");
        if (checkUpdate) {
            CompletableFuture.runAsync(injector.getInstance(UpdateChecker.class)::checkUpdate);
        }
    }
}
