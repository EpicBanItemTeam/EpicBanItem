package com.github.euonmyoji.epicbanitem.configuration;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.item.inventory.AffectItemStackEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class Settings {

    private static final String DEFAULT_WORLD = "default-world";
    private static final String DEFAULT_TRIGGER = "default-trigger";
    private static final String PRINT_ITEM_TO_BLOCK_MAPPING = "print-item-to-block-mapping";
    //    private static final String LISTEN_CHUNK_LOAD = "listen-chunk-load";  not impl yet

    private final Server server = Sponge.getServer();

    @Nullable
    private final Class<? extends AffectItemStackEvent> eventClass;

    private boolean listenLoadingChunk = false;
    private boolean printItemToBlockMapping = true;

    private Map<String, Boolean> enabledWorlds = Maps.newLinkedHashMap();
    private Map<String, Boolean> enabledTriggers = Maps.newLinkedHashMap();

    public Settings(AutoFileLoader fileLoader, Path settingPath) {
        this.resetToDefault();
        fileLoader.addListener(settingPath, this::load, this::save);
        if (Files.notExists(settingPath)) {
            fileLoader.forceSaving(settingPath);
        }
        eventClass = getClassForCraftingResultRedirectionEvent();
    }

    @Nullable
    private Class<? extends AffectItemStackEvent> getClassForCraftingResultRedirectionEvent() {
        try {
            // noinspection SpellCheckingInspection
            String prefix = "com.github.ustc_zzzz.craftingreciperedirector.api";
            return Class.forName(prefix + ".CraftingResultRedirectionEvent").asSubclass(AffectItemStackEvent.class);
        } catch (Exception e) {
            Logger l = EpicBanItem.getLogger();
            PluginManager manager = Sponge.getPluginManager();
            if (manager.getPlugin("FML").isPresent() || manager.getPlugin("fml").isPresent()) {
                // noinspection SpellCheckingInspection
                l.warn("A mod named CraftingResultRedirector is not available on your modded server.");
                l.warn("We recommend installing the mod for filtering and modifying the crafting results.");
                l.warn("It can be downloaded at: https://github.com/ustc-zzzz/CraftingRecipeRedirector/releases");
            }
        }
        return null;
    }

    private void resetToDefault() {
        this.listenLoadingChunk = false;
        this.printItemToBlockMapping = true;
        Collection<WorldProperties> worlds = this.server.getAllWorldProperties();
        this.enabledTriggers = Maps.newLinkedHashMap(Maps.toMap(Triggers.getTriggers().keySet(), k -> true));
        this.enabledWorlds = Maps.newLinkedHashMap(Maps.toMap(Iterables.transform(worlds, WorldProperties::getWorldName), k -> true));
    }

    private void load(ConfigurationNode cfg) {
        this.resetToDefault();

//        this.listenLoadingChunk = cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).getBoolean(false); not impl yet
        this.printItemToBlockMapping = cfg.getNode("epicbanitem", PRINT_ITEM_TO_BLOCK_MAPPING).getBoolean(true);

        ConfigurationNode defaultWorlds = cfg.getNode("epicbanitem", DEFAULT_WORLD);
        defaultWorlds.getChildrenMap().forEach((k, v) -> this.enabledWorlds.put(k.toString(), v.getBoolean()));

        ConfigurationNode defaultTriggers = cfg.getNode("epicbanitem", DEFAULT_TRIGGER);
        defaultTriggers.getChildrenMap().forEach((k, v) -> this.enabledTriggers.put(k.toString(), v.getBoolean()));
    }

    private void save(ConfigurationNode cfg) {
        cfg.getNode("epicbanitem-version").setValue(BanConfig.CURRENT_VERSION);

//        cfg.getNode("epicbanitem", LISTEN_CHUNK_LOAD).setValue(this.listenLoadingChunk); not impl yet
        cfg.getNode("epicbanitem", PRINT_ITEM_TO_BLOCK_MAPPING).setValue(this.printItemToBlockMapping);

        this.enabledWorlds.forEach((k, v) -> cfg.getNode("epicbanitem", DEFAULT_WORLD, k).setValue(v));

        this.enabledTriggers.forEach((k, v) -> cfg.getNode("epicbanitem", DEFAULT_TRIGGER, k).setValue(v));
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
        return this.enabledTriggers.getOrDefault(trigger.toString(), true);
    }

    public boolean isCraftingEventClass(AffectItemStackEvent event) {
        return this.eventClass == null ? event instanceof CraftItemEvent.Preview : this.eventClass.isInstance(event);
    }
}
