/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.InventoryUtils;
import team.ebi.epicbanitem.util.ItemUtils;

public class PlaceRestrictionTrigger extends EBIRestrictionTrigger {
    public PlaceRestrictionTrigger() {
        super(EpicBanItem.key("place"));
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.All event, @Getter("world") ServerWorld world) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        final var carrier = cause.first(Carrier.class);
        event.transactions(Operations.PLACE.get()).forEach(transaction -> {
            final var finalReplacement = transaction.finalReplacement();
            final var location = finalReplacement.location().orElseThrow();
            final var cancelled = new AtomicBoolean(false);
            final var processed =
                    this.processBlockCancellable(event, world, subject, audience, finalReplacement, ignored -> {
                        transaction.invalidate();
                        cancelled.set(true);
                        carrier.ifPresent(it -> ItemUtils.fromBlock(finalReplacement)
                                .ifPresent(item -> InventoryUtils.offerOrDrop(it.inventory(), location, item)));
                    });
            if (processed.isPresent() && !cancelled.get()) {
                final var processedItem = processed.get();
                final var blockType = processedItem.type().block();
                blockType
                        .flatMap(ignored -> ItemUtils.toBlock(processedItem, location, finalReplacement.state()))
                        .ifPresentOrElse(
                                transaction::setCustom,
                                () -> location.spawnEntity(ItemUtils.droppedItem(processedItem, location)));
            }
        });
    }
}
