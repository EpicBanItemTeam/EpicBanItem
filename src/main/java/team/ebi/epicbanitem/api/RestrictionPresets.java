/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.*;

import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;

@RegistryScopes(scopes = RegistryScope.ENGINE)
public final class RestrictionPresets {

    public static final DefaultedRegistryReference<RestrictionPreset> TYPE = key(EpicBanItem.key("type"));

    public static final DefaultedRegistryReference<RestrictionPreset> ALL = key(EpicBanItem.key("all"));

    public static final DefaultedRegistryReference<RestrictionPreset> EMPTY = key(EpicBanItem.key("empty"));

    private RestrictionPresets() {
    }

    public static Registry<RestrictionPreset> registry() {
        return Sponge.server().registry(EBIRegistries.PRESET);
    }

    private static DefaultedRegistryReference<RestrictionPreset> key(final ResourceKey location) {
        return RegistryKey.of(EBIRegistries.PRESET, location).asDefaultedReference(Sponge::server);
    }
}
