/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import java.util.*;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.api.trigger.RestrictionTriggers;

public class TriggerStates extends AbstractMap<ResourceKey, Tristate> implements States {

    private final Map<ResourceKey, Tristate> map;
    private boolean defaultState;

    public TriggerStates(boolean defaultState) {
        this.map = Maps.newHashMap();
        this.update(defaultState);
    }

    public TriggerStates(boolean defaultState, Map<ResourceKey, Tristate> map) {
        this.map = map;
        this.defaultState = defaultState;
        this.update(defaultState);
    }

    @Override
    public Component description(@NotNull ResourceKey key) {
        return RestrictionTriggers.registry().value(key).description();
    }

    @Override
    public Tristate get(Object key) {
        return this.map.get(key);
    }

    @Nullable
    @Override
    public Tristate putIfAbsent(ResourceKey key, Tristate value) {
        return this.map.putIfAbsent(key, value);
    }

    @NotNull
    @Override
    public Set<Entry<ResourceKey, Tristate>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Tristate put(ResourceKey key, Tristate value) {
        return this.map.put(key, value);
    }

    @Override
    public ComponentLike key(ResourceKey key) {
        return RestrictionTriggers.registry().value(key);
    }

    @Override
    public void update(boolean defaultState) {
        this.defaultState = defaultState;
        final var keys = RestrictionTriggers.registry()
                .streamEntries()
                .map(RegistryEntry::key)
                .toList();
        map.keySet().retainAll(keys);
        keys.forEach(key -> map.putIfAbsent(key, Tristate.UNDEFINED));
    }

    @Override
    public boolean defaultState() {
        return defaultState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TriggerStates that = (TriggerStates) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), map);
    }

    public static class Builder extends AbstractDataBuilder<TriggerStates> {

        public Builder() {
            super(TriggerStates.class, 0);
        }

        @Override
        protected Optional<TriggerStates> buildContent(DataView container) throws InvalidDataException {
            return Optional.of(new TriggerStates(
                    container.getBoolean(RestrictionRuleQueries.DEFAULT).orElse(true),
                    container
                            .getMap(RestrictionRuleQueries.STATES)
                            .map(it -> it.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            entry -> ResourceKey.resolve(
                                                    entry.getKey().toString()),
                                            entry -> Tristate.valueOf(
                                                    entry.getValue().toString().toUpperCase()))))
                            .orElse(Maps.newHashMap())));
        }
    }
}
