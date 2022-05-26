/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api;

import java.util.function.Function;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

import org.jetbrains.annotations.NotNull;

@CatalogedBy(RestrictionPresets.class)
@FunctionalInterface
public interface RestrictionPreset extends DefaultedRegistryValue, Function<DataView, DataView> {

    static DataView merge(@NotNull DataView v1, @NotNull DataView v2) {
        DataContainer container = DataContainer.createNew();
        v1.values(false).forEach(container::set);
        v2.values(false).forEach(container::set);
        return container;
    }
}
