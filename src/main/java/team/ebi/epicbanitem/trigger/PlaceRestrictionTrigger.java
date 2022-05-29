/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.SingleTargetRestrictionTrigger;

public class PlaceRestrictionTrigger extends SingleTargetRestrictionTrigger {
    public PlaceRestrictionTrigger() {
        super(EpicBanItem.key("place"));
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.All event) {
        final var audience = event.cause().last(Audience.class).orElse(null);
        final var locale = locale(event.cause());
        event.transactions(Operations.PLACE.get())
                .forEach(transaction -> this.processWithMessage(event, transaction.finalReplacement(), audience, locale)
                        .ifPresent(transaction::setCustom));
    }
}
