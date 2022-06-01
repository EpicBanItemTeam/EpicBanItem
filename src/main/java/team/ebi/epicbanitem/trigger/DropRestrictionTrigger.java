/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class DropRestrictionTrigger extends AbstractRestrictionTrigger {
    public DropRestrictionTrigger() {
        super(EpicBanItem.key("drop"));
    }

    @Listener
    @Include({DropItemEvent.Destruct.class, DropItemEvent.Close.class, DropItemEvent.Custom.class})
    public void onDropItem(final SpawnEntityEvent event) {
        this.handleSpawnEntity(event);
    }
}
