package com.github.euonmyoji.epicbanitem.util;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * @author ustc_zzzz
 */
public class NbtTagDataUtil {

    @SuppressWarnings("deprecation")
    public static DataContainer toNbt(ItemStack stack) {
        DataContainer view = stack.toContainer();
        DataContainer result = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("ItemType")).ifPresent(id -> result.set(DataQuery.of("id"), id));
        view.get(DataQuery.of("UnsafeData")).ifPresent(nbt -> result.set(DataQuery.of("tag"), nbt));
        view.get(DataQuery.of("UnsafeDamage")).ifPresent(damage -> result.set(DataQuery.of("Damage"), damage));

        return result;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack toItemStack(DataView view) {
        DataContainer result = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        result.set(DataQuery.of("Count"), 1);
        view.get(DataQuery.of("id")).ifPresent(id -> result.set(DataQuery.of("ItemType"), id));
        view.get(DataQuery.of("tag")).ifPresent(nbt -> result.set(DataQuery.of("UnsafeData"), nbt));
        view.get(DataQuery.of("Damage")).ifPresent(damage -> result.set(DataQuery.of("UnsafeDamage"), damage));

        return ItemStack.builder().fromContainer(result).build();
    }
}
