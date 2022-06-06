/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroups;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.service.permission.Subject;

import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.InventoryUtils;

@Singleton
public class EquipRestrictionTrigger extends EBIRestrictionTrigger {

    public EquipRestrictionTrigger() {
        super(EpicBanItem.key("equip"));
    }

    @Listener
    public void onChangeEntityEquipment(
            final ChangeEntityEquipmentEvent event,
            final @First Equipable equipable,
            final @First Carrier carrier,
            final @Getter("entity") Entity entity,
            final @Getter("transaction") Transaction<ItemStackSnapshot> transaction,
            final @Getter("slot") EquipmentSlot slot) {
        final var equipmentType = slot.get(Keys.EQUIPMENT_TYPE).orElseThrow();
        final var equipmentGroup = equipmentType.group();
        if (!equipmentGroup.equals(EquipmentGroups.WORN.get())) return;
        final var item = transaction.finalReplacement();
        if (item.isEmpty()) return;
        final var location = entity.serverLocation();
        final var cause = event.cause();
        final var cancelled = new AtomicBoolean(false);
        Optional<ItemStackSnapshot> processed = this.processItemCancellable(
                event,
                location.world(),
                cause.first(Subject.class).orElse(null),
                cause.first(Audience.class).orElse(null),
                item,
                ignored -> cancelled.set(true));

        if (processed.isPresent()) {
            if (cancelled.get()) {
                transaction.setCustom(ItemStackSnapshot.empty());
                location.spawnEntities(InventoryUtils.offerOrDrop(
                        carrier.inventory(), location, processed.get().createStack()));
            } else if (slot.isValidItem(processed.get().type())) transaction.setCustom(processed.get());
            else {
                transaction.setCustom(ItemStackSnapshot.empty());
                location.spawnEntities(InventoryUtils.offerOrDrop(
                        carrier.inventory(), location, processed.get().createStack()));
            }
        } else if (cancelled.get()) {
            location.spawnEntities(InventoryUtils.offerOrDrop(
                    carrier.inventory(),
                    location,
                    transaction.finalReplacement().createStack()));
            transaction.setCustom(ItemStackSnapshot.empty());
        }
    }
}
