/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.util.List;

import org.spongepowered.api.entity.Item;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.StandardInventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.world.server.ServerLocation;

import com.google.common.collect.Lists;

public final class InventoryUtils {
    public static List<Item> offerOrDrop(Inventory inventory, ServerLocation location, ItemStack... item) {
        if (inventory instanceof StandardInventory standardInventory) inventory = standardInventory.primary();
        final var result = inventory.offer(item);
        List<Item> items = Lists.newArrayList();
        if (!result.type().equals(InventoryTransactionResult.Type.SUCCESS))
            items = result.rejectedItems().stream()
                    .map(it -> ItemUtils.droppedItem(it, location))
                    .toList();
        return items;
    }
}
