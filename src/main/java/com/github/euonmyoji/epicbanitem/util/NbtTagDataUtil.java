package com.github.euonmyoji.epicbanitem.util;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author ustc_zzzz
 */
public class NbtTagDataUtil {
    public static void printToLogger(Consumer<String> logger) {
        logger.accept("Generating Item to Block mapping: ");
        Map.ITEM_TO_BLOCK.forEach((k, v) -> {
            Object id = k.getFirst().orElse(null);
            Object damage = k.getSecond().orElse(null);
            logger.accept(String.format("(%s, %s) -> %s", id, damage, v));
        });
    }

    public static DataContainer toNbt(BlockSnapshot snapshot) {
        boolean hasItem = snapshot.getState().getType().getItem().isPresent();
        ItemStack stack = hasItem ? ItemStack.builder().fromBlockSnapshot(snapshot).build() : ItemStack.empty();
        DataContainer container = fromSpongeDataToNbt(stack.toContainer());
        Vector3i position = snapshot.getPosition();
        container.set(DataQuery.of("tag", "BlockEntityTag", "x"), position.getX());
        container.set(DataQuery.of("tag", "BlockEntityTag", "y"), position.getY());
        container.set(DataQuery.of("tag", "BlockEntityTag", "z"), position.getZ());
        return container;
    }

    public static DataContainer toNbt(ItemStackSnapshot stackSnapshot) {
        return fromSpongeDataToNbt(stackSnapshot.toContainer());
    }

    public static DataContainer toNbt(ItemStack stack) {
        return fromSpongeDataToNbt(stack.toContainer());
    }

    public static BlockSnapshot toBlockSnapshot(DataView view, BlockState oldState, UUID worldUniqueId) throws InvalidDataException {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        DataView nbt = view.getView(DataQuery.of("tag", "BlockEntityTag")).orElseThrow(InvalidDataException::new);

        result.set(DataQuery.of("Position", "X"), nbt.get(DataQuery.of("x")).orElseThrow(InvalidDataException::new));
        result.set(DataQuery.of("Position", "Y"), nbt.get(DataQuery.of("y")).orElseThrow(InvalidDataException::new));
        result.set(DataQuery.of("Position", "Z"), nbt.get(DataQuery.of("z")).orElseThrow(InvalidDataException::new));

        result.set(DataQuery.of("WorldUuid"), worldUniqueId.toString());
        result.set(DataQuery.of("UnsafeData"), nbt);

        Set<BlockState> blockStates = findStatesForItemStack(view);

        if (blockStates.contains(oldState)) {
            result.set(DataQuery.of("BlockState"), oldState);
        } else if (!blockStates.isEmpty()) {
            result.set(DataQuery.of("BlockState"), blockStates.iterator().next());
        }

        return BlockSnapshot.builder().build(result).orElseThrow(InvalidDataException::new);
    }

    public static ItemStack toItemStack(DataView view, int stackSize) throws InvalidDataException {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("tag", "SpongeData", "CustomManipulators")).ifPresent(data -> result.set(DataQuery.of("Data"), data));

        view.get(DataQuery.of("id")).ifPresent(id -> result.set(DataQuery.of("ItemType"), id));
        view.get(DataQuery.of("tag")).ifPresent(nbt -> result.set(DataQuery.of("UnsafeData"), nbt));
        view.get(DataQuery.of("Damage")).ifPresent(damage -> result.set(DataQuery.of("UnsafeDamage"), damage));

        result.set(DataQuery.of("Count"), stackSize);

        return ItemStack.builder().build(result).orElseThrow(InvalidDataException::new);
    }

    private static DataContainer fromSpongeDataToNbt(DataContainer view) {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("ItemType")).ifPresent(id -> result.set(DataQuery.of("id"), id));
        view.get(DataQuery.of("UnsafeData")).ifPresent(nbt -> result.set(DataQuery.of("tag"), nbt));
        view.get(DataQuery.of("UnsafeDamage")).ifPresent(damage -> result.set(DataQuery.of("Damage"), damage));

        view.get(DataQuery.of("Data")).ifPresent(data -> result.set(DataQuery.of("tag", "SpongeData", "CustomManipulators"), data));

        return result;
    }

    private static Set<BlockState> findStatesForItemStack(DataView view) {
        return Map.ITEM_TO_BLOCK.get(Tuple.of(view.get(DataQuery.of("id")), view.get(DataQuery.of("Damage"))));
    }

    private static final class Map {
        private static final SetMultimap<Tuple<Optional<?>, Optional<?>>, BlockState> ITEM_TO_BLOCK;

        static {
            SetMultimap<Tuple<Optional<?>, Optional<?>>, BlockState> map;
            map = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
            Collection<BlockType> blocks = Sponge.getRegistry().getAllOf(BlockType.class);
            List<BlockState> statesWithoutCorrespondingItems = new ArrayList<>(blocks.size() * 16);
            statesWithoutCorrespondingItems.add(BlockTypes.AIR.getDefaultState()); // air first
            for (BlockType type : blocks) {
                boolean hasItem = type.getItem().isPresent();
                Stream.concat(Stream.of(type.getDefaultState()), type.getAllBlockStates().stream()).forEach(state -> {
                    try {
                        Preconditions.checkArgument(hasItem);
                        DataContainer nbt = ItemStack.builder().fromBlockState(state).build().toContainer();
                        Optional<Object> damage = nbt.get(DataQuery.of("UnsafeDamage"));
                        Optional<Object> id = nbt.get(DataQuery.of("ItemType"));
                        map.put(Tuple.of(id, damage), state);
                    } catch (Exception e) {
                        statesWithoutCorrespondingItems.add(state);
                    }
                });
            }
            DataContainer nbt = ItemStack.empty().toContainer();
            statesWithoutCorrespondingItems.forEach(state -> {
                Optional<Object> damage = nbt.get(DataQuery.of("UnsafeDamage"));
                Optional<Object> id = nbt.get(DataQuery.of("ItemType"));
                map.put(Tuple.of(id, damage), state);
            });
            ITEM_TO_BLOCK = Multimaps.unmodifiableSetMultimap(map);
        }
    }
}
