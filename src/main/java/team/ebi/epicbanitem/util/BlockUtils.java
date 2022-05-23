/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginManager;

import com.google.common.collect.Maps;
import net.minecraft.world.level.block.FlowerPotBlock;

public final class BlockUtils {

    private static final Map<Predicate<BlockSnapshot>, Function<BlockSnapshot, ItemStack>> BLOCK_TO_ITEM =
            Maps.newHashMap();

    static {
        PluginManager pluginManager = Sponge.pluginManager();
        if (pluginManager.plugin("minecraft").isPresent()) {
            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.TALL_SEAGRASS.get()),
                    ignored -> ItemStack.of(ItemTypes.SEAGRASS.get()));
            BLOCK_TO_ITEM.put(it -> it.state().type() instanceof FlowerPotBlock, block -> ((BlockType)
                    ((FlowerPotBlock) block.state().type()).getContent())
                    .item()
                    .map(ItemStack::of)
                    .orElse(ItemStack.of(ItemTypes.FLOWER_POT)));
        }
    }

    private BlockUtils() {
    }

    public static Optional<ItemStack> fromBlock(BlockSnapshot snapshot) {
        try {
            return Optional.of(BLOCK_TO_ITEM.entrySet().stream()
                    .filter(it -> it.getKey().test(snapshot))
                    .map(it -> it.getValue().apply(snapshot))
                    .findAny()
                    .orElseGet(() ->
                            ItemStack.builder().fromBlockSnapshot(snapshot).build()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
