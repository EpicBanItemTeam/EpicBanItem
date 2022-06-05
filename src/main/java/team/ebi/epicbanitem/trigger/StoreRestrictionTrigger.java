/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.TransferInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.entity.StandardInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.InventoryUtils;

@Singleton
public class StoreRestrictionTrigger extends EBIRestrictionTrigger {

    private static final Predicate<SlotTransaction> IS_STANDARD_INVENTORY =
            it -> it.slot().viewedSlot().parent() instanceof StandardInventory;

    public StoreRestrictionTrigger() {
        super(EpicBanItem.key("store"));
    }

    // Handle Dropper and Hopper, Hopper minecart not implemented
    @Listener
    public void onTransferInventory(
            final TransferInventoryEvent.Post event,
            final @First ServerWorld world,
            final @ContextValue("CREATOR") UUID creator,
            final @Getter("transferredItem") ItemStackSnapshot transferredItem,
            final @Getter("sourceSlot") Slot sourceSlot,
            final @Getter("targetSlot") Slot targetSlot) {
        final var entityOpt = world.entity(creator);
        if (entityOpt.isEmpty()) return;
        final var cancelled = new AtomicBoolean(false);
        final var processed = this.processItemCancellable(
                event,
                world,
                entityOpt
                        .filter(Subject.class::isInstance)
                        .map(Subject.class::cast)
                        .orElse(null),
                entityOpt
                        .filter(Audience.class::isInstance)
                        .map(Audience.class::cast)
                        .orElse(null),
                transferredItem,
                ignored -> {
                    targetSlot.poll(transferredItem.quantity());
                    cancelled.set(true);
                });
        if (processed.isPresent()) {
            if (cancelled.get()) sourceSlot.offer(processed.get().createStack());
            else {
                targetSlot.poll(transferredItem.quantity());
                targetSlot.parent().offer(processed.get().createStack());
            }
        } else if (cancelled.get()) sourceSlot.offer(transferredItem.createStack());
    }

    @Listener
    @Exclude({
        ClickContainerEvent.Creative.class,
        ClickContainerEvent.Drop.class,
        ClickContainerEvent.Recipe.class,
        ClickContainerEvent.NumberPress.class,
        ClickContainerEvent.SelectTrade.class
    })
    public void onClickContainer(
            ClickContainerEvent event,
            @Getter("inventory") Container container,
            @First ServerPlayer player,
            @Getter("transactions") List<SlotTransaction> transactions,
            @Getter("cursorTransaction") Transaction<ItemStackSnapshot> cursorTransaction) {
        final var world = player.world();
        final var location = player.serverLocation();
        final var cancelled = new AtomicBoolean(false);
        final var containerSlots =
                transactions.stream().filter(IS_STANDARD_INVENTORY.negate()).toList();
        final var fallbackInventory = event.inventory().children().stream()
                .filter(it -> it instanceof PrimaryPlayerInventory)
                .findAny()
                .or(() -> transactions.stream()
                        .map(it -> it.slot().viewedSlot().parent())
                        .filter(it -> it instanceof StandardInventory)
                        .map(it -> ((StandardInventory) it).primary())
                        .findAny())
                .orElse(event.inventory());
        final var cursorOriginal = cursorTransaction.original();
        final var cursorFinal = cursorTransaction.finalReplacement();
        final var isCursor = cursorOriginal.quantity() > cursorFinal.quantity();
        // Put in container
        containerSlots.stream()
                .filter(it -> it.original().quantity() < it.finalReplacement().quantity())
                .forEach(transaction -> {
                    final var originalItem = transaction.original();
                    final var finalItem = transaction.finalReplacement();
                    final var deltaItem = ItemStack.builder()
                            .fromSnapshot(finalItem)
                            .quantity(finalItem.quantity() - originalItem.quantity())
                            .build()
                            .createSnapshot();
                    final var processed =
                            this.processItemCancellable(event, world, player, player, deltaItem, ignored -> {
                                cancelled.set(true);
                            });
                    if (processed.isPresent()) {
                        if (cancelled.get()) {
                            if (isCursor) {
                                transaction.invalidate();
                                cursorTransaction.setCustom(ItemStack.builder()
                                        .fromSnapshot(processed.get())
                                        .quantity(cursorOriginal.quantity())
                                        .build()
                                        .createSnapshot());
                            } else {
                                transaction.setCustom(ItemStack.empty());
                                InventoryUtils.offerOrDrop(
                                        fallbackInventory,
                                        location,
                                        processed.get().createStack());
                            }
                        } else transaction.setCustom(processed.get().createStack());
                    } else if (cancelled.get()) event.setCancelled(true);
                });
    }
}
