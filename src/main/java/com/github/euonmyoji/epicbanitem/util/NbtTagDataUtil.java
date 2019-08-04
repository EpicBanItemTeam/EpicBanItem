package com.github.euonmyoji.epicbanitem.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class NbtTagDataUtil {
    private static final DataQuery ID = DataQuery.of("id");
    private static final DataQuery DAMAGE = DataQuery.of("Damage");
    private static final DataQuery BLOCK_ENTITY_TAG = DataQuery.of("tag", "BlockEntityTag");

    private static final DataQuery COUNT = DataQuery.of("Count");
    private static final DataQuery POSITION = DataQuery.of("Position");
    private static final DataQuery ITEM_TYPE = DataQuery.of("ItemType");
    private static final DataQuery WORLD_UUID = DataQuery.of("WorldUuid");
    private static final DataQuery UNSAFE_DATA = DataQuery.of("UnsafeData");
    private static final DataQuery BLOCK_STATE = DataQuery.of("BlockState");
    private static final DataQuery UNSAFE_DAMAGE = DataQuery.of("UnsafeDamage");

    private static final DataQuery UNSAFE_DATA_ITEM = DataQuery.of("UnsafeData", "Item");

    private static final Server SERVER = Sponge.getServer();
    private static final AtomicBoolean LOGGED = new AtomicBoolean(false);

    public static void printToLogger(Consumer<String> logger, boolean verbose) {
        if (!LOGGED.getAndSet(true)) {
            int countOfBlockStates = 0;
            Optional<UUID> defaultWorldUUID = SERVER.getDefaultWorld().map(WorldProperties::getUniqueId);
            BiFunction<Location<World>, BlockState, ItemStack> pickBlockGetter = Map.getPickBlockGetter();
            Location<World> dummyLocation = new Location<>(defaultWorldUUID.flatMap(SERVER::getWorld).get(), 0, 0, 0);
            for (BlockType type : Sponge.getRegistry().getAllOf(BlockType.class)) {
                Collection<BlockState> states = type.getAllBlockStates();
                countOfBlockStates += states.size();
                for (BlockState state : states) {
                    DataContainer itemData = pickBlockGetter.apply(dummyLocation, state).toContainer();
                    if (verbose) {
                        Object id = itemData.get(ITEM_TYPE).orElse(null);
                        Object damage = itemData.get(UNSAFE_DAMAGE).orElse(null);
                        logger.accept(String.format("(%s, %s) -> %s", id, damage, state));
                    }
                }
            }
            logger.accept("Successfully tested item to block mapping (" + countOfBlockStates + " entries).");
        }
    }

    public static World getDefaultWorld() {
        WorldProperties defProps = Sponge.getServer().getDefaultWorld().orElseThrow(RuntimeException::new);
        return Sponge.getServer().getWorld(defProps.getUniqueId()).orElseThrow(RuntimeException::new);
    }

    public static String getId(DataView view) throws InvalidDataException {
        return view.getString(ID).orElseThrow(() -> invalidData(view));
    }

    public static DataContainer toNbt(BlockSnapshot snapshot) {
        ItemStack item = snapshot.getLocation().map(Map::getItemByBlock).orElse(ItemStack.empty());
        DataContainer result = toNbt(item.createSnapshot());

        DataView nbt = snapshot.toContainer().getView(UNSAFE_DATA).orElse(DataContainer.createNew());
        return result.set(BLOCK_ENTITY_TAG, snapshot.getPosition()).set(BLOCK_ENTITY_TAG, nbt);
    }

    public static DataContainer toNbt(ItemStackSnapshot stackSnapshot) {
        World world = getDefaultWorld();
        Entity entity = world.createEntity(EntityTypes.ITEM, Vector3d.ZERO);

        entity.offer(Keys.REPRESENTED_ITEM, stackSnapshot);
        DataView.SafetyMode safetyMode = DataView.SafetyMode.NO_DATA_CLONED;
        Optional<DataView> data = entity.toContainer().getView(UNSAFE_DATA_ITEM);
        Supplier<DataContainer> def = () -> DataContainer.createNew(safetyMode).set(ID, "minecraft:air");

        return data.map(v -> v.copy(safetyMode)).orElseGet(def).remove(COUNT);
    }

    public static DataContainer toNbt(ItemStack stack) {
        return toNbt(stack.createSnapshot());
    }

    public static BlockSnapshot toBlockSnapshot(DataView view, UUID worldUniqueId) throws InvalidDataException {
        Vector3i position = view.getObject(BLOCK_ENTITY_TAG, Vector3i.class).orElseThrow(() -> invalidData(view));
        DataContainer result = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        World world = SERVER.getWorld(worldUniqueId).orElseThrow(() -> invalidData(view));

        result.set(UNSAFE_DATA, view.get(BLOCK_ENTITY_TAG).orElse(DataContainer.createNew()));
        result.set(BLOCK_STATE, Map.getBlockByItem(view, new Location<>(world, position)));
        result.set(WORLD_UUID, worldUniqueId.toString());
        result.set(POSITION.then("X"), position.getX());
        result.set(POSITION.then("Y"), position.getY());
        result.set(POSITION.then("Z"), position.getZ());

        return BlockSnapshot.builder().build(result).orElseThrow(() -> invalidData(view));
    }

    public static ItemStack toItemStack(DataView view, int stackSize) throws InvalidDataException {
        World world = getDefaultWorld();

        Entity entity = world.createEntity(EntityTypes.ITEM, Vector3d.ZERO);
        DataContainer data = entity.toContainer();
        data.set(UNSAFE_DATA_ITEM, view);

        Optional<ItemStackSnapshot> optional = world.createEntity(data).flatMap(i -> i.get(Keys.REPRESENTED_ITEM));
        ItemStack result = optional.orElseThrow(() -> invalidData(view)).createStack();
        result.setQuantity(stackSize);

        return result;
    }

    private static InvalidDataException invalidData(DataView dataView) {
        return new InvalidDataException("InvalidData: " + TextUtil.serializeNbtToString(dataView).toPlain());
    }

    private static final class Map {
        private static final BiFunction<Location<World>, BlockState, ItemStack> GETTER = getPickBlockGetter();
        private static final LoadingCache<Location<World>, ImmutableMap<BlockState, ItemStack>> CACHE = getCache();

        private static BlockState getBlockByItem(DataView view, Location<World> location) throws InvalidDataException {
            long bestSimilarity = -1;
            BlockState oldState = location.getBlock(), bestState = BlockSnapshot.NONE.getState();
            ImmutableMap<BlockState, ItemStack> map = Objects.requireNonNull(CACHE.get(location));
            for (java.util.Map.Entry<BlockState, ItemStack> entry : map.entrySet()) {
                DataContainer d = entry.getValue().toContainer();
                if (d.get(ITEM_TYPE).equals(view.get(ID)) && d.get(UNSAFE_DAMAGE).equals(view.get(DAMAGE))) {
                    Predicate<BlockTrait<?>> p = t -> entry.getKey().getTraitValue(t).equals(oldState.getTraitValue(t));
                    long newSimilarity = oldState.getTraitMap().keySet().stream().filter(p).count();
                    if (newSimilarity > bestSimilarity) {
                        bestSimilarity = newSimilarity;
                        bestState = entry.getKey();
                    }
                }
            }
            if (bestSimilarity < 0) {
                Optional<BlockType> typeOptional = view.getCatalogType(ID, ItemType.class).flatMap(ItemType::getBlock);
                for (BlockState state : typeOptional.orElseThrow(() -> invalidData(view)).getAllBlockStates()) {
                    DataContainer d = GETTER.apply(location, state).toContainer();
                    if (d.get(ITEM_TYPE).equals(view.get(ID)) && d.get(UNSAFE_DAMAGE).equals(view.get(DAMAGE))) {
                        Predicate<BlockTrait<?>> p = t -> state.getTraitValue(t).equals(oldState.getTraitValue(t));
                        long similarity = oldState.getTraitMap().keySet().stream().filter(p).count();
                        if (similarity > bestSimilarity) {
                            bestSimilarity = similarity;
                            bestState = state;
                        }
                    }
                }
            }
            return bestState;
        }

        private static ItemStack getItemByBlock(Location<World> location) {
            ImmutableMap<BlockState, ItemStack> map = Objects.requireNonNull(CACHE.get(location));
            return map.getOrDefault(location.getBlock(), ItemStack.empty());
        }

        private static LoadingCache<Location<World>, ImmutableMap<BlockState, ItemStack>> getCache() {
            return Caffeine.newBuilder().weakKeys().expireAfterWrite(30, TimeUnit.MINUTES).build(location -> {
                ImmutableMap.Builder<BlockState, ItemStack> builder = ImmutableMap.builder();
                for (BlockState state : location.getBlockType().getAllBlockStates()) {
                    builder.put(state, GETTER.apply(location, state));
                }
                return builder.build();
            });
        }

        private static BiFunction<Location<World>, BlockState, ItemStack> getPickBlockGetter() {
            try {
                return getPickBlockGetterUnsafe();
            } catch (ReflectiveOperationException e) {
                String msg = "Cannot find internal methods and fallback method for mapping blocks will be used";
                EpicBanItem.getLogger().warn(msg, e);
                return getPickBlockGetterFallback();
            }
        }

        private static BiFunction<Location<World>, BlockState, ItemStack> getPickBlockGetterUnsafe() throws ReflectiveOperationException {
            // TODO: Add compatibility of Forge servers by using net.minecraft.block.Block#getPickBlock instead
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> itemStackUtil = Class.forName("org.spongepowered.common.item.inventory.util.ItemStackUtil");
            Class<?> iBlockState = Class.forName("net.minecraft.block.state.IBlockState");
            Class<?> blockPos = Class.forName("net.minecraft.util.math.BlockPos");
            Class<?> itemStack = Class.forName("net.minecraft.item.ItemStack");
            Class<?> world = Class.forName("net.minecraft.world.World");
            Class<?> block = Class.forName("net.minecraft.block.Block");

            String getItemName = "func_185473_a";
            String fromNativeName = "fromNative";

            MethodType getItemType = MethodType.methodType(itemStack, BlockType.class, World.class, blockPos, BlockState.class);
            MethodType newBlockPosType = MethodType.methodType(void.class, int.class, int.class, int.class);
            MethodType getItemTypeOld = MethodType.methodType(itemStack, world, blockPos, iBlockState);
            MethodType fromNativeType = MethodType.methodType(ItemStack.class, itemStack);

            MethodHandle fromNativeMethod = lookup.findStatic(itemStackUtil, fromNativeName, fromNativeType);
            MethodHandle newBlockPosMethod = lookup.findConstructor(blockPos, newBlockPosType);

            MethodHandle getItem3Method = lookup.findVirtual(block, getItemName, getItemTypeOld).asType(getItemType);
            MethodHandle getItem2Method = MethodHandles.collectArguments(getItem3Method, 2, newBlockPosMethod);
            MethodHandle getItemMethod = MethodHandles.filterReturnValue(getItem2Method, fromNativeMethod);

            return (location, state) -> {
                try {
                    World extent = location.getExtent();
                    int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
                    return (ItemStack) getItemMethod.invokeExact(state.getType(), extent, x, y, z, state);
                } catch (Throwable ignored) {
                    return getPickBlockGetterFallback().apply(location, state);
                }
            };
        }

        private static BiFunction<Location<World>, BlockState, ItemStack> getPickBlockGetterFallback() {
            return (location, state) -> {
                try {
                    BlockType type = state.getType();
                    type.getItem().orElseThrow(InvalidDataException::new);
                    return ItemStack.builder().fromBlockState(state).build();
                } catch (Exception ignored) {
                    return ItemStack.empty();
                }
            };
        }
    }
}
