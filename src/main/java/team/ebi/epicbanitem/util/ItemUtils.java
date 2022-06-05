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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import com.google.common.collect.Maps;
import net.minecraft.world.level.block.FlowerPotBlock;
import team.ebi.epicbanitem.api.ItemQueries;

public final class ItemUtils {

    private static final Map<Predicate<BlockSnapshot>, Function<BlockSnapshot, Optional<ItemStack>>> BLOCK_TO_ITEM =
            Maps.newHashMap();

    private static final DataQuery BLOCK_ENTITY_TAG = ItemQueries.UNSAFE_DATA.then(ItemQueries.BLOCK_ENTITY_TAG);

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

    private ItemUtils() {}

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

    public static Optional<BlockSnapshot> toBlock(
            final ItemStackSnapshot item, final ServerLocation location, final BlockState oldState) {
        return item.type().block().flatMap(blockType -> {
            final var state =
                    BlockState.builder().blockType(blockType).addFrom(oldState).build();
            final var container = item.toContainer();
            var block = Optional.<BlockSnapshot>empty();
            final var blockEntityType = BlockEntityTypes.registry().findValue(blockType.key(RegistryTypes.BLOCK_TYPE));
            if (container.contains(BLOCK_ENTITY_TAG) && blockEntityType.isPresent()) {
                final var archetype = BlockEntityArchetype.builder()
                        .state(state)
                        .blockEntity(blockEntityType.get())
                        .blockEntityData(container.getView(BLOCK_ENTITY_TAG).orElseThrow())
                        .build();
                block = Optional.of(archetype.toSnapshot(location));
            } else {
                block = Optional.of(
                        BlockSnapshot.builder().from(location).blockState(state).build());
            }
            return block;
        });
    }
}
