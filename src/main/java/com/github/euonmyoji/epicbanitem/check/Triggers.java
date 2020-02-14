package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public final class Triggers implements AdditionalCatalogRegistryModule<CheckRuleTrigger> {
    public static final Triggers RegisterModule = new Triggers();

    public static final Impl USE = new Impl("use");
    public static final Impl EQUIP = new Impl("equip");
    public static final Impl CRAFT = new Impl("craft");
    public static final Impl PICKUP = new Impl("pickup");
    public static final Impl CLICK = new Impl("click");
    public static final Impl THROW = new Impl("throw");
    public static final Impl DROP = new Impl("drop");
    public static final Impl PLACE = new Impl("place");
    public static final Impl BREAK = new Impl("break");
    public static final Impl INTERACT = new Impl("interact");
    public static final Impl JOIN = new Impl("join");

    private static final SortedMap<String, CheckRuleTrigger> triggers = new TreeMap<>();

    private Triggers() {}

    public static SortedMap<String, CheckRuleTrigger> getTriggers() {
        return Sponge.getRegistry().getAllOf(CheckRuleTrigger.class).stream()
                .collect(Collectors.toMap(CatalogType::getId, Function.identity(), (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); }, TreeMap::new));
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
        Stream.of(USE, EQUIP, CRAFT, PICKUP, CLICK, THROW, DROP, PLACE, BREAK, INTERACT, JOIN).forEach(this::registerAdditionalCatalog);
    }

    @Override
    public void registerAdditionalCatalog(CheckRuleTrigger extraCatalog) {
        triggers.put(extraCatalog.getId(), extraCatalog);
    }

    @NonnullByDefault
    public static final class Impl implements CheckRuleTrigger {
        private final String name;

        private Impl(String name) {
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
            return EpicBanItem.getMessages().getMessage("epicbanitem.triggers." + toString().toLowerCase(Locale.ROOT));
        }

        @Override
        public String getId() {
            return EpicBanItem.PLUGIN_ID + ":" + name.toLowerCase(Locale.ROOT);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
