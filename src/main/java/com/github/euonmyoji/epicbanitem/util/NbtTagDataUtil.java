package com.github.euonmyoji.epicbanitem.util;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
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

    public static Optional<DataContainer> toNbt(BlockSnapshot snapshot) {
        if (snapshot.getState().getType().getItem().isPresent()) {
            ItemStack stack = ItemStack.builder().fromBlockSnapshot(snapshot).build();
            DataContainer container = fromSpongeDataToNbt(stack.toContainer());
            Optional<Location<World>> optional = snapshot.getLocation();
            if (optional.isPresent()) {
                Location<World> location = optional.get();
                container.set(DataQuery.of("tag", "BlockEntityTag", "x"), location.getX());
                container.set(DataQuery.of("tag", "BlockEntityTag", "y"), location.getY());
                container.set(DataQuery.of("tag", "BlockEntityTag", "z"), location.getZ());
            } else {
                container.remove(DataQuery.of("tag", "BlockEntityTag", "x"));
                container.remove(DataQuery.of("tag", "BlockEntityTag", "y"));
                container.remove(DataQuery.of("tag", "BlockEntityTag", "z"));
            }
            return Optional.of(container);
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

    public static BlockSnapshot toBlockSnapshot(DataView view, BlockState oldState, World world) throws InvalidDataException {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("tag", "BlockEntityTag")).ifPresent(nbt -> result.set(DataQuery.of("UnsafeData"), nbt));

        result.set(DataQuery.of("Position", "X"), view.get(DataQuery.of("tag", "BlockEntityTag", "x")));
        result.set(DataQuery.of("Position", "Y"), view.get(DataQuery.of("tag", "BlockEntityTag", "y")));
        result.set(DataQuery.of("Position", "Z"), view.get(DataQuery.of("tag", "BlockEntityTag", "z")));

        result.set(DataQuery.of("WorldUuid"), world.getUniqueId());

        Collection<BlockState> blockStates = findStatesForItemStack(view);

        if (blockStates.contains(oldState)) {
            result.set(DataQuery.of("BlockState"), oldState.toContainer());
        } else if (blockStates.isEmpty()) {
            result.set(DataQuery.of("BlockState"), BlockSnapshot.NONE.getState());
        } else {
            result.set(DataQuery.of("BlockState"), blockStates.iterator().next());
        }

        // noinspection ConstantConditions
        return BlockSnapshot.builder().build(result).orElse(BlockSnapshot.NONE);
    }

    public static ItemStack toItemStack(DataView view, int stackSize) throws InvalidDataException {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("tag", "SpongeData", "CustomManipulators")).ifPresent(data -> result.set(DataQuery.of("Data"), data));

        view.get(DataQuery.of("id")).ifPresent(id -> result.set(DataQuery.of("ItemType"), id));
        view.get(DataQuery.of("tag")).ifPresent(nbt -> result.set(DataQuery.of("UnsafeData"), nbt));
        view.get(DataQuery.of("Damage")).ifPresent(damage -> result.set(DataQuery.of("UnsafeDamage"), damage));

        result.set(DataQuery.of("Count"), stackSize);

        return ItemStack.builder().build(result).orElse(ItemStack.empty());
    }

    private static DataContainer fromSpongeDataToNbt(DataContainer view) {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(DataQuery.of("ItemType")).ifPresent(id -> result.set(DataQuery.of("id"), id));
        view.get(DataQuery.of("UnsafeData")).ifPresent(nbt -> result.set(DataQuery.of("tag"), nbt));
        view.get(DataQuery.of("UnsafeDamage")).ifPresent(damage -> result.set(DataQuery.of("Damage"), damage));

        view.get(DataQuery.of("Data")).ifPresent(data -> result.set(DataQuery.of("tag", "SpongeData", "CustomManipulators"), data));

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
                        try {
                            DataContainer nbt = ItemStack.builder().fromBlockState(state).build().toContainer();
                            Optional<Object> damage = nbt.get(DataQuery.of("UnsafeDamage"));
                            Optional<Object> id = nbt.get(DataQuery.of("ItemType"));
                            map.put(Tuple.of(id, damage), state);
                        } catch (Exception e) {
                            EpicBanItem.logger.error("Failed to get itemstack form " + state, e);
                        }
                    }
                }
            }
            ITEM_TO_BLOCK = map.build();
        }
    }
}
