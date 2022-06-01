/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class BeInteractedRestrictionTrigger extends AbstractRestrictionTrigger {
    public BeInteractedRestrictionTrigger() {
        super(EpicBanItem.key("be_interacted"));
    }

    @Listener
    @Include({InteractBlockEvent.Primary.Start.class, InteractBlockEvent.Secondary.class})
    public void onInteractBlock(InteractBlockEvent event, @Getter("block") BlockSnapshot block) {
        // Will trigger on both hands
        final var cause = event.cause();
        this.processCancellable(
                        event,
                        block.location().map(ServerLocation::world).orElseThrow(),
                        cause.last(Subject.class).orElse(null),
                        cause.last(Audience.class).orElse(null),
                        block)
                .ifPresent(it -> it.restore(true, BlockChangeFlags.NONE));
    }
}
