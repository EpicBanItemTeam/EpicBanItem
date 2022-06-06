/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;

import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.minecraft.world.inventory.CraftingContainer;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.InventoryUtils;
import team.ebi.epicbanitem.util.ItemUtils;

@Singleton
public class CraftRestrictionTrigger extends EBIRestrictionTrigger {

    public CraftRestrictionTrigger() {
        super(EpicBanItem.key("craft"));
    }

    @Listener
    public void onCraftItemPreview(
            final CraftItemEvent.Preview event,
            final @Getter("preview") SlotTransaction preview,
            final @Getter("craftingInventory") CraftingInventory craftingInventory,
            final @First Locatable locatable) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        final var inventory = cause.allOf(Carrier.class).stream()
                .map(Carrier::inventory)
                .distinct()
                .filter(it -> !(it instanceof Container container)
                        || container.viewed().stream().noneMatch(CraftingContainer.class::isInstance))
                .findAny();
        final var location = locatable.serverLocation();
        final var world = location.world();
        final var gridInventory = craftingInventory.craftingGrid();
        gridInventory.slots().stream().filter(it -> it.freeCapacity() == 0).forEach(it -> {
            final var cancelled = new AtomicBoolean(false);
            final var processed = this.processItemCancellable(
                    event, world, subject, audience, it.peek().createSnapshot(), ignored -> {
                        cancelled.set(true);
                        preview.invalidate();
                    });
            if (processed.isPresent()) {
                if (cancelled.get()) {
                    it.set(ItemStack.empty());
                    if (inventory.isPresent())
                        location.spawnEntities(InventoryUtils.offerOrDrop(
                                inventory.get(), location, processed.get().createStack()));
                    else location.spawnEntity(ItemUtils.droppedItem(processed.get(), location));
                } else it.set(processed.get().createStack());
            } else if (cancelled.get()) {
                if (inventory.isPresent())
                    location.spawnEntities(InventoryUtils.offerOrDrop(inventory.get(), location, it.peek()));
                else location.spawnEntity(ItemUtils.droppedItem(it.peek().createSnapshot(), location));
                it.set(ItemStack.empty());
            }
        });
    }
}
