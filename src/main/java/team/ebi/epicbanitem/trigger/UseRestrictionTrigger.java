/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.Optional;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.Locatable;

import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.SingleTargetRestrictionTrigger;

public class UseRestrictionTrigger extends SingleTargetRestrictionTrigger {
    public UseRestrictionTrigger() {
        super(EpicBanItem.key("use"));
    }

    @Listener
    public void onInteractItem(
            InteractItemEvent.Secondary event,
            @Last Locatable locatable,
            @Last Equipable equipable,
            @Getter("itemStack") ItemStackSnapshot item,
            @ContextValue("USED_HAND") HandType hand) {
        EquipmentType equipment = EquipmentTypes.registry().value(hand.key(RegistryTypes.HAND_TYPE));
        Optional<Slot> slot = equipable.equipment().slot(equipment);
        if (slot.isEmpty()) return;
        this.processWithMessage(event, item).ifPresent(it -> slot.get().set(it.createStack()));
    }
}
