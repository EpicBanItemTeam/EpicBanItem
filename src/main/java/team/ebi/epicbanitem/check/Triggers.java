package team.ebi.epicbanitem.check;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.plugin.meta.util.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckRuleTrigger;
import team.ebi.epicbanitem.locale.LocaleService;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public final class Triggers implements AdditionalCatalogRegistryModule<CheckRuleTrigger> {
    public static final CheckRuleTrigger USE = new Impl("use");
    public static final CheckRuleTrigger EQUIP = new Impl("equip");
    public static final CheckRuleTrigger CRAFT = new Impl("craft");
    public static final CheckRuleTrigger PICKUP = new Impl("pickup");
    public static final CheckRuleTrigger CLICK = new Impl("click");
    public static final CheckRuleTrigger THROW = new Impl("throw");
    public static final CheckRuleTrigger DROP = new Impl("drop");
    public static final CheckRuleTrigger PLACE = new Impl("place");
    public static final CheckRuleTrigger BREAK = new Impl("break");
    public static final CheckRuleTrigger INTERACT = new Impl("interact");
    public static final CheckRuleTrigger JOIN = new Impl("join");
    public static final CheckRuleTrigger STORE = new Impl("store");

    private static final SortedMap<String, CheckRuleTrigger> triggers = new TreeMap<>();

    @Inject
    public Triggers(EventManager eventManager, PluginContainer pluginContainer) {
        eventManager.registerListeners(pluginContainer, this);
    }

    public static SortedMap<String, CheckRuleTrigger> getTriggers() {
        return Sponge
            .getRegistry()
            .getAllOf(CheckRuleTrigger.class)
            .stream()
            .collect(
                Collectors.toMap(
                    CatalogType::getId,
                    Function.identity(),
                    (u, v) -> {
                        throw new IllegalStateException(String.format("Duplicate key %s", u));
                    },
                    TreeMap::new
                )
            );
    }

    @Override
    public Optional<CheckRuleTrigger> getById(String id) {
        return Optional.ofNullable(triggers.get(id));
    }

    @Override
    public Collection<CheckRuleTrigger> getAll() {
        return ImmutableSet.copyOf(triggers.values());
    }

    @Override
    public void registerDefaults() {
        Stream.of(USE, EQUIP, CRAFT, PICKUP, CLICK, THROW, DROP, PLACE, BREAK, INTERACT, JOIN, STORE).forEach(this::registerAdditionalCatalog);
    }

    @Override
    public void registerAdditionalCatalog(CheckRuleTrigger extraCatalog) {
        triggers.put(extraCatalog.getId(), extraCatalog);
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        Sponge.getRegistry().registerModule(CheckRuleTrigger.class, this);
    }

    @NonnullByDefault
    public static final class Impl implements CheckRuleTrigger {
        private final String name;
        private final String id;

        private Impl(String name) {
            this.name = name;
            this.id = EpicBanItem.PLUGIN_ID + ":" + name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public Text toText() {
            String key = "epicbanitem.triggers." + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
            LocaleService localeService = EpicBanItem.getLocaleService();
            return localeService
                .getTextWithFallback(key)
                .toBuilder()
                .onHover(TextActions.showText(localeService.getTextWithFallback(key + ".description")))
                .build();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
