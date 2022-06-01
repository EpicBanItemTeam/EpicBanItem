/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class PlaceRestrictionTrigger extends AbstractRestrictionTrigger {
    public PlaceRestrictionTrigger() {
        super(EpicBanItem.key("place"));
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.All event, @Getter("world") ServerWorld world) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        event.transactions(Operations.PLACE.get()).forEach(transaction -> this.processCancellable(
                        event, world, subject, audience, transaction.finalReplacement())
                .ifPresent(transaction::setCustom));
    }
}
