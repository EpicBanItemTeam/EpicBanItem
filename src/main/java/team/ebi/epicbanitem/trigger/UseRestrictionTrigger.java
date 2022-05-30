/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Locatable;

import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class UseRestrictionTrigger extends AbstractRestrictionTrigger {
    public UseRestrictionTrigger() {
        super(EpicBanItem.key("use"));
    }

    @Listener
    public void onInteractItem(
            InteractItemEvent.Secondary event,
            @Last Locatable locatable,
            @Last Equipable equipable,
            @ContextValue("USED_HAND") HandType hand,
            @Getter("itemStack") ItemStackSnapshot item) {
        this.handleInteract(event, equipable, hand, item);
    }
}
