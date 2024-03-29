/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;

import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;

@Singleton
public class BeCraftedRestrictionTrigger extends EBIRestrictionTrigger {

    public BeCraftedRestrictionTrigger() {
        super(EpicBanItem.key("be_crafted"));
    }

    @Listener
    public void onCraftItemPreview(
            final CraftItemEvent.Preview event,
            final @Getter("preview") SlotTransaction preview,
            final @First Locatable locatable) {
        final var cause = event.cause();
        final var cancelled = new AtomicBoolean(false);
        final var processed = this.processItemCancellable(
                event,
                locatable.serverLocation().world(),
                cause.first(Subject.class).orElse(null),
                cause.first(Audience.class).orElse(null),
                preview.finalReplacement(),
                ignored -> cancelled.set(true));
        if (cancelled.get()) preview.setCustom(ItemStack.empty());
        else processed.ifPresent(preview::setCustom);
    }
}
