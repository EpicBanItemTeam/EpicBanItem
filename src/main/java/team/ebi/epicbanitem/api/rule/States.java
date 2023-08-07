/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import java.util.Map;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

public interface States extends Map<ResourceKey, Tristate>, DataSerializable {
    ComponentLike key(ResourceKey key);

    default Component description(@NotNull ResourceKey key) {
        return Component.text(key.asString());
    }

    void update(boolean defaultState);

    default boolean getOrDefault(ResourceKey key) {
        final var result = get(key).asNullableBoolean();
        return result == null ? defaultState() : result;
    }

    boolean defaultState();

    @Override
    default DataContainer toContainer() {
        return DataContainer.createNew()
                .set(RestrictionRuleQueries.DEFAULT, defaultState())
                // Avoid recursive
                .set(RestrictionRuleQueries.STATES, Maps.newHashMap(this));
    }

    @Override
    default int contentVersion() {
        return 0;
    }
}
