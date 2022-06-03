/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;

public class UseRestrictionTrigger extends EBIRestrictionTrigger {
    public UseRestrictionTrigger() {
        super(EpicBanItem.key("use"));
    }

    @Listener
    public void onInteractItem(
            InteractItemEvent.Secondary event,
            @Last Locatable locatable,
            @Last Equipable equipable,
            @ContextValue("USED_HAND") HandType hand,
            @ContextValue("USED_ITEM") ItemStackSnapshot item) {
        final var cause = event.cause();
        slotFromHand(equipable, hand).ifPresent(it -> this.processCancellable(
                        event,
                        locatable.serverLocation().world(),
                        cause.first(Subject.class).orElse(null),
                        cause.first(Audience.class).orElse(null),
                        item)
                .map(ItemStackSnapshot::createStack)
                .ifPresent(it::set));
    }
}
