/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.InventoryUtils;
import team.ebi.epicbanitem.util.ItemUtils;

public class PickupRestrictionTrigger extends EBIRestrictionTrigger {
    public PickupRestrictionTrigger() {
        super(EpicBanItem.key("pickup"));
    }

    @Listener
    public void onChangeInventoryPickup(
            final ChangeInventoryEvent.Pickup event,
            final @Getter("inventory") Inventory inventory,
            final @Getter("transactions") List<SlotTransaction> transactions,
            final @First Locatable locatable) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        final var location = locatable.serverLocation();
        final var world = location.world();
        for (final var transaction : transactions) {
            final var originalItem = transaction.original();
            final var finalItem = transaction.finalReplacement();
            final var pickedItem = ItemStack.builder()
                    .fromSnapshot(finalItem)
                    .quantity(finalItem.quantity() - originalItem.quantity())
                    .build()
                    .createSnapshot();
            final var cancelled = new AtomicBoolean(false);
            final var processed = this.processItemCancellable(
                    event, world, subject, audience, pickedItem, ignored -> cancelled.set(true));
            if (processed.isPresent()) {
                transaction.setCustom(originalItem);
                if (cancelled.get()) {
                    location.spawnEntity(ItemUtils.droppedItem(processed.get(), location));
                } else
                    InventoryUtils.offerOrDrop(
                            inventory, location, processed.get().createStack());
            }
        }
    }
}
