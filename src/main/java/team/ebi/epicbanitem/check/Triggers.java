package team.ebi.epicbanitem.check;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.util.NonnullByDefault;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckRuleTrigger;

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

        public Impl(String name) { // TODO: api
            this.name = name;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            return this == that || that instanceof CheckRuleTrigger && this.name.equals(that.toString());
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public Text toText() {
            return EpicBanItem.getLocaleService()
                    .getText("epicbanitem.triggers." + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name))
                    .orElse(Text.of(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name)));
        }

        @Override
        public String getId() {
            return EpicBanItem.PLUGIN_ID + ":" + name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
