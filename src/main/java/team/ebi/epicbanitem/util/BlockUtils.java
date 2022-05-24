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
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginManager;

import com.google.common.collect.Maps;
import net.minecraft.world.level.block.FlowerPotBlock;

public final class BlockUtils {

    private static final Map<Predicate<BlockSnapshot>, Function<BlockSnapshot, Optional<ItemStack>>> BLOCK_TO_ITEM =
            Maps.newHashMap();

    static {
        PluginManager pluginManager = Sponge.pluginManager();
        if (pluginManager.plugin("minecraft").isPresent()) {
            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.TALL_SEAGRASS.get()),
                    ignored -> Optional.ofNullable(ItemStack.of(ItemTypes.SEAGRASS.get())));

            BLOCK_TO_ITEM.put(
                    it -> it.state().type() instanceof FlowerPotBlock,
                    block -> Optional.ofNullable(
                            ((BlockType) ((FlowerPotBlock) block.state().type()).getContent())
                                    .item()
                                    .map(ItemStack::of)
                                    .orElse(ItemStack.of(ItemTypes.FLOWER_POT))));

            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.BAMBOO_SAPLING.get()),
                    ignored -> Optional.ofNullable(ItemStack.of(ItemTypes.BAMBOO.get())));
            // GrowingPlantBodyBlock
            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.KELP_PLANT.get()),
                    ignored -> Optional.ofNullable(ItemStack.of(ItemTypes.KELP.get())));
            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.TWISTING_VINES_PLANT.get()),
                    ignored -> Optional.ofNullable(ItemStack.of(ItemTypes.TWISTING_VINES.get())));
            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.WEEPING_VINES_PLANT.get()),
                    ignored -> Optional.ofNullable(ItemStack.of(ItemTypes.WEEPING_VINES.get())));

            BLOCK_TO_ITEM.put(
                    it -> it.state().type().equals(BlockTypes.PISTON_HEAD.get()),
                    block -> block.state().get(Keys.PISTON_TYPE).map(it -> {
                        if (it.equals(PistonTypes.NORMAL.get())) {
                            return ItemStack.of(ItemTypes.PISTON);
                        } else if (it.equals(PistonTypes.STICKY.get())) {
                            return ItemStack.of(ItemTypes.STICKY_PISTON);
                        } else return null;
                    }));
        }
    }

    private BlockUtils() {}

    public static Optional<ItemStack> fromBlock(BlockSnapshot snapshot) {
        try {
            return Optional.of(BLOCK_TO_ITEM.entrySet().stream()
                    .filter(it -> it.getKey().test(snapshot))
                    .map(it -> it.getValue().apply(snapshot))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny()
                    .orElseGet(() ->
                            ItemStack.builder().fromBlockSnapshot(snapshot).build()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
