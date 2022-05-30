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
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;
import team.ebi.epicbanitem.util.EventUtils;

public class BreakRestrictionTrigger extends AbstractRestrictionTrigger {
    public BreakRestrictionTrigger() {
        super(EpicBanItem.key("break"));
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.All event) {
        final var cause = event.cause();
        final var audience = cause.last(Audience.class).orElse(null);
        final var locale = EventUtils.locale(cause);
        event.transactions(Operations.BREAK.get())
                .forEach(transaction -> this.process(event, transaction.finalReplacement(), audience, locale)
                        .ifPresent(transaction::setCustom));
    }
}
