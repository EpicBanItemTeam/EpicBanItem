/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import com.google.common.util.concurrent.Atomics;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class ThrowRestrictionTrigger extends AbstractRestrictionTrigger {
    public ThrowRestrictionTrigger() {
        super(EpicBanItem.key("throw"));
    }

    @Listener
    public void onDropItemDispense(final DropItemEvent.Dispense event) {
        final var cause = event.cause();
        final var audience = cause.last(Audience.class).orElse(null);
        final var subject = cause.last(Subject.class).orElse(null);
        final var hasInventory = event instanceof ChangeInventoryEvent;
        final var world = Atomics.<ServerWorld>newReference(null);
        event.filterEntities(entity -> {
            if (world.get() == null) world.set(entity.serverLocation().world());
            if (!(entity instanceof Item item)) return true;
            final var mutable = item.item();
            final var snapshot = mutable.get();
            final var cancelled = new AtomicBoolean(false);
            final var processed = !hasInventory
                    ? this.process(event, world.get(), subject, audience, snapshot)
                    : this.processCancellable(event, world.get(), subject, audience, snapshot, ignored -> {
                        ((ChangeInventoryEvent) event).filter(it -> !snapshot.equals(it.createSnapshot()));
                        cancelled.set(true);
                    });
            if (processed.isPresent()) {
                item.offer(mutable.set(processed.get()));
                if (hasInventory && cancelled.get())
                    ((ChangeInventoryEvent) event)
                            .inventory()
                            .offer(processed.get().createStack());
            }

            return !cancelled.get();
        });
    }
}
