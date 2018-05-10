package com.github.euonmyoji.epicbanitem.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public class NbtTagDataUtil {
    public static Iterator<String> printLog() {
        return Map.ITEM_TO_BLOCK.entries().stream().map(e -> {
            BlockState states = e.getValue();
            Object id = e.getKey().getFirst().orElse(null);
            Object damage = e.getKey().getSecond().orElse(null);
            return String.format("(%s, %s) -> %s", id, damage, states);
        }).iterator();
    }

    public static Optional<DataContainer> toNbt(BlockSnapshot snapshot) {
        if (snapshot.getState().getType().getItem().isPresent()) {
            ItemStack stack = ItemStack.builder().fromBlockSnapshot(snapshot).build();
            return Optional.of(fromSpongeDataToNbt(stack.toContainer()));
        } else {
            return Optional.empty();
        }
    }

    public static DataContainer toNbt(ItemStackSnapshot stackSnapshot) {
        return fromSpongeDataToNbt(stackSnapshot.toContainer());
    }

    public static DataContainer toNbt(ItemStack stack) {
        return fromSpongeDataToNbt(stack.toContainer());
    }

    @SuppressWarnings("deprecation")
    public static BlockSnapshot toBlockSnapshot(DataView view, BlockState oldState, Location<World> location) {
        DataContainer result = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("tag", "BlockEntityTag")).ifPresent(nbt -> result.set(DataQuery.of("UnsafeData"), nbt));

        view.set(DataQuery.of("WorldUuid"), location.getExtent().getUniqueId());
        view.set(DataQuery.of("Position", "X"), location.getBlockX());
        view.set(DataQuery.of("Position", "Y"), location.getBlockY());
        view.set(DataQuery.of("Position", "Z"), location.getBlockZ());

        Collection<BlockState> blockStates = findStatesForItemStack(view);

        if (blockStates.contains(oldState)) {
            view.set(DataQuery.of("BlockState"), oldState.toContainer());
        } else if (blockStates.isEmpty()) {
            view.set(DataQuery.of("BlockState"), BlockSnapshot.NONE.getState());
        } else {
            view.set(DataQuery.of("BlockState"), blockStates.iterator().next());
        }

        // noinspection ConstantConditions
        return BlockSnapshot.builder().build(view).get();
    }

    @SuppressWarnings("deprecation")
    public static ItemStack toItemStack(DataView view) {
        DataContainer result = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        result.set(DataQuery.of("Count"), 1);
        view.get(DataQuery.of("id")).ifPresent(id -> result.set(DataQuery.of("ItemType"), id));
        view.get(DataQuery.of("tag")).ifPresent(nbt -> result.set(DataQuery.of("UnsafeData"), nbt));
        view.get(DataQuery.of("Damage")).ifPresent(damage -> result.set(DataQuery.of("UnsafeDamage"), damage));

        return ItemStack.builder().build(result).orElseGet(ItemStack::empty);
    }

    @SuppressWarnings("deprecation")
    private static DataContainer fromSpongeDataToNbt(DataContainer view) {
        DataContainer result = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("ItemType")).ifPresent(id -> result.set(DataQuery.of("id"), id));
        view.get(DataQuery.of("UnsafeData")).ifPresent(nbt -> result.set(DataQuery.of("tag"), nbt));
        view.get(DataQuery.of("UnsafeDamage")).ifPresent(damage -> result.set(DataQuery.of("Damage"), damage));
        return result;
    }

    private static Collection<BlockState> findStatesForItemStack(DataView view) {
        return Map.ITEM_TO_BLOCK.get(Tuple.of(view.get(DataQuery.of("id")), view.get(DataQuery.of("Damage"))));
    }

    private static final class Map {
        private static final Multimap<Tuple<Optional<?>, Optional<?>>, BlockState> ITEM_TO_BLOCK;

        static {
            ImmutableMultimap.Builder<Tuple<Optional<?>, Optional<?>>, BlockState> map = ImmutableMultimap.builder();
            for (BlockType blockType : Sponge.getRegistry().getAllOf(BlockType.class)) {
                if (blockType.getItem().isPresent()) {
                    for (BlockState state : blockType.getAllBlockStates()) {
                        DataContainer nbt = ItemStack.builder().fromBlockState(state).build().toContainer();
                        Optional<Object> damage = nbt.get(DataQuery.of("UnsafeDamage"));
                        Optional<Object> id = nbt.get(DataQuery.of("ItemType"));
                        map.put(Tuple.of(id, damage), state);
                    }
                }
            }
            ITEM_TO_BLOCK = map.build();
        }
    }
}
