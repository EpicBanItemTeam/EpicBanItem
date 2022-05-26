/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import team.ebi.epicbanitem.EpicBanItem;

/**
 * Recommend to extend {@link AbstractRestrictionTrigger}
 */
@CatalogedBy(RestrictionTriggers.class)
public interface RestrictionTrigger extends DefaultedRegistryValue, ResourceKeyed, ComponentLike {
    String CONTEXT_KEY = EpicBanItem.NAMESPACE + "-trigger";

    Component description();
}
