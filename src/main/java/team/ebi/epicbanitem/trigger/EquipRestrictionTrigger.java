/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;

import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

@Singleton
public class EquipRestrictionTrigger extends AbstractRestrictionTrigger {

    public EquipRestrictionTrigger() {
        super(EpicBanItem.key("equip"));
    }

    @Listener
    public void onChangeEntityEquipment(
            final ChangeEntityEquipmentEvent event,
            @Last Equipable equipable,
            @Getter("entity") Entity entity,
            @Getter("transaction") Transaction<ItemStackSnapshot> transaction) {
        final var item = transaction.finalReplacement();
        final var cause = event.cause();
        if (item.isEmpty()) return;
        // TODO change the cancel
        this.processCancellable(
                        event,
                        entity.serverLocation().world(),
                        cause.last(Subject.class).orElse(null),
                        cause.last(Audience.class).orElse(null),
                        item)
                .ifPresent(transaction::setCustom);
    }
}
