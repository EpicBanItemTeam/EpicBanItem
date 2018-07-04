package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.SimpleCheckRuleServiceImpl;
import com.github.euonmyoji.epicbanitem.command.EpicBanItemCommand;
import com.github.euonmyoji.epicbanitem.configuration.BanConfig;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.listener.ChunkListener;
import com.github.euonmyoji.epicbanitem.listener.InventoryListener;
import com.github.euonmyoji.epicbanitem.listener.WorldItemMoveListener;
import com.github.euonmyoji.epicbanitem.message.Messages;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author EpicBanItem Team
 */
@Plugin(id = "epicbanitem", name = "EpicBanItem", version = EpicBanItem.VERSION, authors = {"yinyangshi", "GiNYAi", "ustc-zzzz"},
        description = "a banitem plugin with nbt")
public class EpicBanItem {
    public static EpicBanItem plugin;
    static final String VERSION = "1.0";

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;

    public static Logger logger;

    private Messages messages;

    public Messages getMessages() {
        return messages;
    }

    private Settings settings;

    public Settings getSettings() {
        return settings;
    }

    private BanConfig banConfig;

    private SimpleCheckRuleServiceImpl service;

    @Inject
    public void setLogger(Logger logger) {
        EpicBanItem.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        service = new SimpleCheckRuleServiceImpl();
        Sponge.getServiceManager().setProvider(this,CheckRuleService.class,service);
        messages = new Messages(this, cfgDir);
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        logger.debug("Item to Block matching: ");
        NbtTagDataUtil.printLog().forEachRemaining(log -> logger.debug(log));
        try {
            reload();
        } catch (IOException|ObjectMappingException e) {
            logger.warn("Failed to load epicbanitem", e);
        }
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, EpicBanItemCommand.ebi, "epicbanitem", "ebi", "banitem");
        Sponge.getEventManager().registerListeners(this, new InventoryListener());
        Sponge.getEventManager().registerListeners(this, new WorldItemMoveListener());
        Sponge.getEventManager().registerListeners(this, new ChunkListener());
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        try {
            reload();
        } catch (IOException|ObjectMappingException e) {
            logger.warn("IOException when reload epicbanitem", e);
        }
    }

    public void reload() throws IOException, ObjectMappingException {
        //todo:更好的异常处理?
        logger.info("reloading");
        Files.createDirectories(cfgDir);
        if (settings == null) {
            settings = new Settings(cfgDir.resolve("settings.conf"));
        }
        settings.reload();
        //example
        Optional<Asset> exampleAsset = Sponge.getAssetManager().getAsset(this,"example_check_rules.conf");
        if(exampleAsset.isPresent()){
            try {
                exampleAsset.get().copyToFile(cfgDir.resolve("example.conf"),true);
            }catch (IOException e){
                logger.warn("Failed to copy example ban config.",e);
            }
        }else {
            logger.warn("Cannot find example ban config.");
        }
        if(banConfig==null){
            banConfig = new BanConfig(cfgDir.resolve("banitem.conf"),true);
        }
        banConfig.reload();
        Map<ItemType,List<CheckRule>> rules = service.getRules();
        service.clear();
        service.addRules(BanConfig.findType(banConfig.getRules()));
        logger.info("reloaded");
    }

}
