package com.github.euonmyoji.epicbanitem;

import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.check.listener.ChunkListener;
import com.github.euonmyoji.epicbanitem.check.listener.InventoryListener;
import com.github.euonmyoji.epicbanitem.check.listener.WorldListener;
import com.github.euonmyoji.epicbanitem.command.CommandEbi;
import com.github.euonmyoji.epicbanitem.configuration.BanConfig;
import com.github.euonmyoji.epicbanitem.configuration.Settings;
import com.github.euonmyoji.epicbanitem.locale.LocaleService;
import com.google.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@Plugin(
    id = EpicBanItem.PLUGIN_ID,
    name = EpicBanItem.NAME,
    version = EpicBanItem.VERSION,
    dependencies = @Dependency(id = Platform.API_ID, version = "7.1.0"),
    authors = {"yinyangshi", "GiNYAi", "ustc-zzzz"},
    description = "A banitem with nbt plugin in sponge"
)
public class EpicBanItem {

    public static final String PLUGIN_ID = "epicbanitem";
    public static final String NAME = "EpicBanItem";
    public static final String VERSION = "@version@";

    private static EpicBanItem instance;

    @Inject
    private Logger logger;

    private final LocaleService localeService;
    private final Settings settings;

    @Inject
    public EpicBanItem(
        Settings settings,
        LocaleService localeService,
        BanConfig banConfig,
        ChunkListener chunkListener,
        InventoryListener inventoryListener,
        WorldListener worldListener,
        CommandEbi commandEbi,
        Triggers triggers
    ) {
        instance = this;

        this.localeService = localeService;
        this.settings = settings;

        Objects.requireNonNull(settings);
        Objects.requireNonNull(localeService);
        Objects.requireNonNull(banConfig);
        Objects.requireNonNull(chunkListener);
        Objects.requireNonNull(inventoryListener);
        Objects.requireNonNull(worldListener);
        Objects.requireNonNull(banConfig);
        Objects.requireNonNull(commandEbi);
        Objects.requireNonNull(triggers);
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static LocaleService getLocaleService() {
        return instance.localeService;
    }

    public static Settings getSettings() {
        return instance.settings;
    }
}
