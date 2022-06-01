/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class ThrowRestrictionTrigger extends AbstractRestrictionTrigger {
    public ThrowRestrictionTrigger() {
        super(EpicBanItem.key("throw"));
    }

    @Listener
    public void onDropItemDispense(final DropItemEvent.Dispense event) {
        this.handleSpawnEntity(event);
    }
}