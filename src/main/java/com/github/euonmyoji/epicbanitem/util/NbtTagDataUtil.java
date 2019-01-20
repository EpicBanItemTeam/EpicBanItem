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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class NbtTagDataUtil {
    private static final DataQuery ID = DataQuery.of("id");
    private static final DataQuery TAG = DataQuery.of("tag");
    private static final DataQuery DAMAGE = DataQuery.of("Damage");
    private static final DataQuery BLOCK_ENTITY_TAG = DataQuery.of("tag", "BlockEntityTag");
    private static final DataQuery CUSTOM_MANIPULATORS = DataQuery.of("tag", "SpongeData", "CustomManipulators");

    private static final DataQuery DATA = DataQuery.of("Data");
    private static final DataQuery COUNT = DataQuery.of("Count");
    private static final DataQuery POSITION = DataQuery.of("Position");
    private static final DataQuery ITEM_TYPE = DataQuery.of("ItemType");
    private static final DataQuery WORLD_UUID = DataQuery.of("WorldUuid");
    private static final DataQuery UNSAFE_DATA = DataQuery.of("UnsafeData");
    private static final DataQuery BLOCK_STATE = DataQuery.of("BlockState");
    private static final DataQuery UNSAFE_DAMAGE = DataQuery.of("UnsafeDamage");

    public static void printToLogger(Consumer<String> logger, boolean verbose) {
        logger.accept("Successfully generated item to block mapping (" + Map.ITEM_TO_BLOCK.size() + " entries).");
        if (verbose) {
            Map.ITEM_TO_BLOCK.forEach((k, v) -> {
                Object id = k.getFirst().orElse(null);
                Object damage = k.getSecond().orElse(null);
                logger.accept(String.format("(%s, %s) -> %s", id, damage, v));
            });
        }
    }

    public static DataContainer toNbt(BlockSnapshot snapshot) {
        Vector3i position = snapshot.getPosition();

        DataContainer result = fromSpongeDataToNbt(Map.getItemByBlock(snapshot.getState()).orElseGet(() -> ItemStack.empty().toContainer()));

        snapshot.toContainer().getView(UNSAFE_DATA).ifPresent(nbt -> result.set(BLOCK_ENTITY_TAG, nbt));

        result.set(BLOCK_ENTITY_TAG.then("x"), position.getX());
        result.set(BLOCK_ENTITY_TAG.then("y"), position.getY());
        result.set(BLOCK_ENTITY_TAG.then("z"), position.getZ());

        return result;
    }

    public static DataContainer toNbt(ItemStackSnapshot stackSnapshot) {
        return fromSpongeDataToNbt(stackSnapshot.toContainer());
    }

    public static DataContainer toNbt(ItemStack stack) {
        return fromSpongeDataToNbt(stack.toContainer());
    }

    public static BlockSnapshot toBlockSnapshot(DataView view, BlockState oldState, UUID worldUniqueId) throws InvalidDataException {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        Map.getBlockByItem(view, oldState).ifPresent(state -> result.set(BLOCK_STATE, state));

        view.get(BLOCK_ENTITY_TAG.then("x")).ifPresent(x -> result.set(POSITION.then("X"), x));
        view.get(BLOCK_ENTITY_TAG.then("y")).ifPresent(y -> result.set(POSITION.then("Y"), y));
        view.get(BLOCK_ENTITY_TAG.then("z")).ifPresent(z -> result.set(POSITION.then("Z"), z));

        view.get(BLOCK_ENTITY_TAG).ifPresent(nbt -> result.set(UNSAFE_DATA, nbt));

        result.set(WORLD_UUID, worldUniqueId.toString());

        return BlockSnapshot.builder().build(result).orElseThrow(() -> invalidData(view));
    }

    public static ItemStack toItemStack(DataView view, int stackSize) throws InvalidDataException {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(CUSTOM_MANIPULATORS).ifPresent(data -> result.set(DATA, data));

        view.get(ID).ifPresent(id -> result.set(ITEM_TYPE, id));
        view.get(TAG).ifPresent(nbt -> result.set(UNSAFE_DATA, nbt));
        view.get(DAMAGE).ifPresent(damage -> result.set(UNSAFE_DAMAGE, damage));

        result.set(COUNT, stackSize);

        return ItemStack.builder().build(result).orElseThrow(() -> invalidData(view));
    }

    private static InvalidDataException invalidData(DataView dataView) {
        return new InvalidDataException("InvalidData: " + TextUtil.serializeNbtToString(dataView).toPlain());
    }

    private static DataContainer fromSpongeDataToNbt(DataContainer view) {
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);

        view.get(ITEM_TYPE).ifPresent(id -> result.set(ID, id));
        view.get(UNSAFE_DATA).ifPresent(nbt -> result.set(TAG, nbt));
        view.get(UNSAFE_DAMAGE).ifPresent(damage -> result.set(DAMAGE, damage));

        view.get(DATA).ifPresent(data -> result.set(CUSTOM_MANIPULATORS, data));

        return result;
    }

    private static final class Map {
        private static final SetMultimap<Tuple<Optional<?>, Optional<?>>, BlockState> ITEM_TO_BLOCK;

        static {
            SetMultimap<Tuple<Optional<?>, Optional<?>>, BlockState> map;
            map = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
            Stream.concat(BlockTypes.AIR.getAllBlockStates().stream(), Sponge.getRegistry().getAllOf(BlockType.class).stream()
                    .flatMap(type -> Stream.concat(Stream.of(type.getDefaultState()), type.getAllBlockStates().stream()))).forEach(state -> {
                DataContainer nbt = getItemByBlock(state).orElseGet(() -> ItemStack.empty().toContainer());
                map.put(Tuple.of(nbt.get(ITEM_TYPE), nbt.get(UNSAFE_DAMAGE)), state);
            });
            ITEM_TO_BLOCK = Multimaps.unmodifiableSetMultimap(map);
        }

        private static Optional<BlockState> getBlockByItem(DataView view, BlockState oldState) {
            Set<BlockState> blockStates = ITEM_TO_BLOCK.get(Tuple.of(view.get(ID), view.get(DAMAGE)));
            if (blockStates.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(blockStates.contains(oldState) ? oldState : blockStates.iterator().next());
            }
        }

        private static Optional<DataContainer> getItemByBlock(BlockState state) {
            try {
                Preconditions.checkArgument(state.getType().getItem().isPresent());
                return Optional.of(ItemStack.builder().fromBlockState(state).build().toContainer());
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
}
