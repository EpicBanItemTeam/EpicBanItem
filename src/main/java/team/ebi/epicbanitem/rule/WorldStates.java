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

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.rule.RestrictionRule.States;

public class WorldStates extends AbstractMap<ResourceKey, Boolean> implements States<ResourceKey> {

    private final Map<ResourceKey, Boolean> map;

    public WorldStates(boolean defaultState) {
        this.map = Maps.newHashMap();
        this.update(defaultState);
    }

    public WorldStates(Map<ResourceKey, Boolean> map) {
        this.map = map;
    }

    @Override
    public Boolean get(Object key) {
        return map.get(key);
    }

    @NotNull
    @Override
    public Set<Entry<ResourceKey, Boolean>> entrySet() {
        return map.entrySet();
    }

    @Override
    public ComponentLike key(ResourceKey key) {
        return Component.text(key.asString());
    }

    @Override
    public void update(boolean defaultState) {
        clear();
        Sponge.server().worldManager().worlds().forEach(world -> put(world.key(), defaultState));
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
}
