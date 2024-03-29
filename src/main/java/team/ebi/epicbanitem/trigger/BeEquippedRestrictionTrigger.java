/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

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
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;

@Singleton
public class BeEquippedRestrictionTrigger extends EBIRestrictionTrigger {
    @Inject
    private PluginContainer plugin;

    public BeEquippedRestrictionTrigger() {
        super(EpicBanItem.key("be_equipped"));
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
        final var item = transaction.original();
        final var cause = event.cause();
        if (item.isEmpty()) return;
        // TODO Use click rewrite
        final var cancelled = new AtomicBoolean(false);
        final var processed = this.processItemCancellable(
                event,
                entity.serverLocation().world(),
                cause.first(Subject.class).orElse(null),
                cause.first(Audience.class).orElse(null),
                item,
                ignored -> {
                    cancelled.set(true);
                    event.setCancelled(true);
                    //                    carrier.inventory().offer(transaction.finalReplacement().createStack());
                });
        if (processed.isPresent()) {
            if (cancelled.get()) {
                //                Sponge.server().scheduler().submit(Task.builder().plugin(plugin).execute(() ->
                // slot.set(processed.get().createStack())).build());
            } else {

            }
        }
    }
}
