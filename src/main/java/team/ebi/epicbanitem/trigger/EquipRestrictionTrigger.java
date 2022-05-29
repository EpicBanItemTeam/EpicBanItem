/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Locatable;

import com.google.inject.Singleton;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.SingleTargetRestrictionTrigger;

@Singleton
public class EquipRestrictionTrigger extends SingleTargetRestrictionTrigger {

    public EquipRestrictionTrigger() {
        super(EpicBanItem.key("equip"));
    }

    @Listener
    public void onChangeEntityEquipment(
            final ChangeEntityEquipmentEvent event,
            @Last Locatable locatable,
            @Last Equipable equipable,
            @Getter("transaction") Transaction<ItemStackSnapshot> transaction) {
        final var item = transaction.finalReplacement();
        if (item.isEmpty()) return;
        this.processWithMessage(event, item).ifPresent(transaction::setCustom);
    }
}
