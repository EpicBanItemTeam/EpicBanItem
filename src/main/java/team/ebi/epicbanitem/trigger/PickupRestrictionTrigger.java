/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.List;

import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;

public class PickupRestrictionTrigger extends AbstractRestrictionTrigger {
    public PickupRestrictionTrigger() {
        super(EpicBanItem.key("pickup"));
    }

    @Listener
    public void onChangeInventoryPickup(
            final ChangeInventoryEvent.Pickup.Pre event,
            @Getter("originalStack") final ItemStackSnapshot snapshot,
            @Getter("item") Item item) {
        final var cause = event.cause();
        this.processCancellable(
                        event,
                        item.serverLocation().world(),
                        cause.last(Subject.class).orElse(null),
                        cause.last(Audience.class).orElse(null),
                        snapshot)
                .ifPresent(it -> event.setCustom(List.of(it)));
    }
}
