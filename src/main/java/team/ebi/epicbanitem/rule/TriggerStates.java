/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.rule;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.api.RestrictionTriggers;
import team.ebi.epicbanitem.api.rule.RestrictionRule.States;

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
        clear();
        RestrictionTriggers.registry()
                .streamEntries()
                .map(RegistryEntry::key)
                .forEach(key -> map.putIfAbsent(key, Tristate.UNDEFINED));
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
}
