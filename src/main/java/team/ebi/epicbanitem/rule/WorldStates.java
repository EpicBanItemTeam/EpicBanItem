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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.api.rule.RestrictionRule.States;

public class WorldStates extends AbstractMap<ResourceKey, Tristate> implements States {

    private final Map<ResourceKey, Tristate> map;
    private boolean defaultState;

    public WorldStates(boolean defaultState) {
        this.map = Maps.newHashMap();
        this.update(defaultState);
    }

    public WorldStates(boolean defaultState, Map<ResourceKey, Tristate> map) {
        this.map = map;
        this.defaultState = defaultState;
        this.update(defaultState);
    }

    @Override
    public Tristate get(Object key) {
        return this.map.get(key);
    }

    @Override
    public Tristate put(ResourceKey key, Tristate value) {
        return this.map.put(key, value);
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
    public ComponentLike key(ResourceKey key) {
        return Component.text(key.asString());
    }

    @Override
    public void update(boolean defaultState) {
        this.defaultState = defaultState;
        clear();
        Sponge.server().worldManager().worlds().forEach(world -> put(world.key(), Tristate.UNDEFINED));
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
        WorldStates that = (WorldStates) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), map);
    }

    @Override
    public boolean defaultState() {
        return defaultState;
    }
}
