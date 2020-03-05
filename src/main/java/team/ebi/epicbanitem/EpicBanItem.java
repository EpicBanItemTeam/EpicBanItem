package team.ebi.epicbanitem;

import com.google.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.check.listener.ChunkListener;
import team.ebi.epicbanitem.check.listener.InventoryListener;
import team.ebi.epicbanitem.check.listener.ThrowListener;
import team.ebi.epicbanitem.check.listener.WorldListener;
import team.ebi.epicbanitem.command.CommandEbi;
import team.ebi.epicbanitem.configuration.BanConfig;
import team.ebi.epicbanitem.configuration.Settings;
import team.ebi.epicbanitem.locale.LocaleService;

/**
 * @author The EpicBanItem Team
 */
@Plugin(
    id = EpicBanItem.PLUGIN_ID,
    name = EpicBanItem.NAME,
    version = EpicBanItem.VERSION,
    dependencies = @Dependency(id = Platform.API_ID, version = "7.1.0"),
    authors = {"yinyangshi", "GiNYAi", "ustc-zzzz", "SettingDust"},
    description = "The sponge plugin for item restriction by checking nbt tags"
)
public class EpicBanItem {

    public static final String PLUGIN_ID = "epicbanitem";
    public static final String NAME = "EpicBanItem";
    public static final String VERSION = "@version@";
    public static final String ORE = "https://ore.spongepowered.org/EpicBanItem/EpicBanItem";

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
        ThrowListener throwListener,
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
