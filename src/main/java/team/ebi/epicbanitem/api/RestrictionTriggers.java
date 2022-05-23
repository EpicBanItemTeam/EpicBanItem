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
public final class RestrictionTriggers {

    public static final DefaultedRegistryReference<RestrictionTrigger> USE = key(EpicBanItem.key("use"));
    public static final DefaultedRegistryReference<RestrictionTrigger> INTERACT = key(EpicBanItem.key("interact"));

    public static final DefaultedRegistryReference<RestrictionTrigger> EQUIP = key(EpicBanItem.key("equip"));
    public static final DefaultedRegistryReference<RestrictionTrigger> CRAFT = key(EpicBanItem.key("craft"));
    public static final DefaultedRegistryReference<RestrictionTrigger> STORE = key(EpicBanItem.key("store"));
    public static final DefaultedRegistryReference<RestrictionTrigger> CLICK = key(EpicBanItem.key("click"));

    public static final DefaultedRegistryReference<RestrictionTrigger> PICKUP = key(EpicBanItem.key("pickup"));
    public static final DefaultedRegistryReference<RestrictionTrigger> THROW = key(EpicBanItem.key("throw"));
    public static final DefaultedRegistryReference<RestrictionTrigger> DROP = key(EpicBanItem.key("drop"));

    public static final DefaultedRegistryReference<RestrictionTrigger> PLACE = key(EpicBanItem.key("place"));
    public static final DefaultedRegistryReference<RestrictionTrigger> BREAK = key(EpicBanItem.key("break"));

    public static final DefaultedRegistryReference<RestrictionTrigger> JOIN = key(EpicBanItem.key("join"));

    private RestrictionTriggers() {}

    public static Registry<RestrictionTrigger> registry() {
        return Sponge.server().registry(EBIRegistries.TRIGGER);
    }

    private static DefaultedRegistryReference<RestrictionTrigger> key(final ResourceKey location) {
        return RegistryKey.of(EBIRegistries.TRIGGER, location).asDefaultedReference(Sponge::server);
    }
}
