/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import java.util.Map;
import java.util.Objects;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.util.Tristate;

import net.kyori.adventure.text.ComponentLike;

public interface States extends Map<ResourceKey, Tristate> {
    ComponentLike key(ResourceKey key);

    void update(boolean defaultState);

    default boolean getOrDefault(ResourceKey key) {
        return Objects.requireNonNullElse(get(key).asNullableBoolean(), defaultState());
    }

    boolean defaultState();
}
