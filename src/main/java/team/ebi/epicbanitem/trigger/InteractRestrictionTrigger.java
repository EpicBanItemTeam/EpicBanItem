/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class InteractRestrictionTrigger extends AbstractRestrictionTrigger {
    public InteractRestrictionTrigger() {
        super(EpicBanItem.key("interact"));
    }

    @Listener
    @Include({InteractEntityEvent.class, InteractBlockEvent.Primary.Start.class, InteractBlockEvent.Secondary.class})
    public void onInteract(
            InteractEvent event,
            @Last Equipable equipable,
            @ContextValue("USED_HAND") HandType hand,
            @ContextValue("USED_ITEM") ItemStackSnapshot item) {
        this.handleInteract(event, equipable, hand, item);
    }
}
