/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;

import com.google.common.collect.Sets;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;

public class DropRestrictionTrigger extends EBIRestrictionTrigger {
    public DropRestrictionTrigger() {
        super(EpicBanItem.key("drop"));
    }

    // Sponge not implement these DropItemEvent

    @Listener
    public void onDropItemPre(
            final DropItemEvent.Pre event,
            @First Locatable locatable,
            @Getter("droppedItems") List<ItemStackSnapshot> drops) {
        final var cause = event.cause();
        final var audience = cause.last(Audience.class).orElse(null);
        final var subject = cause.last(Subject.class).orElse(null);
        final var world = locatable.serverLocation().world();
        final var finalDrops = Sets.<ItemStackSnapshot>newHashSet();
        for (final var drop : drops) {
            final var cancelled = new AtomicBoolean(false);
            Optional<ItemStackSnapshot> processed =
                    this.processItemCancellable(event, world, subject, audience, drop, ignored -> cancelled.set(true));
            if (processed.isPresent() && !cancelled.get()) finalDrops.add(processed.get());
            else if (!cancelled.get()) finalDrops.add(drop);
        }
        drops.clear();
        drops.addAll(finalDrops);
    }

    //    @Listener
    //    @Include({DropItemEvent.Destruct.class, DropItemEvent.Close.class, DropItemEvent.Custom.class})
    //    public void onDropItem(final SpawnEntityEvent event) {
    //        final var cause = event.cause();
    //        final var audience = cause.last(Audience.class).orElse(null);
    //        final var subject = cause.last(Subject.class).orElse(null);
    //        final var world = Atomics.<ServerWorld>newReference(null);
    //        event.filterEntities(entity -> {
    //            if (world.get() == null) world.set(entity.serverLocation().world());
    //            if (!(entity instanceof Item item)) return true;
    //            final var mutable = item.item();
    //            final var cancelled = new AtomicBoolean(false);
    //            this.processCancellable(
    //                            event, world.get(), subject, audience, mutable.get(), ignored -> cancelled.set(true))
    //                    .ifPresent(itemStackSnapshot -> item.offer(mutable.set(itemStackSnapshot)));
    //            return !cancelled.get();
    //        });
    //    }
}
