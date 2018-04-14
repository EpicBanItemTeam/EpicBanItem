package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author ustc_zzzz
 */
public final class NbtTypeHelper {
    @Nullable
    public static Object getObject(DataQuery query, DataView view) {
        Object result = view;
        for (String queryPart : query.getParts()) {
            Map<String, ?> map = getAsMap(result);
            if (Objects.nonNull(map)) {
                result = map.get(queryPart);
                if (Objects.nonNull(result)) {
                    continue;
                }
            }
            Integer index = Types.asInt(queryPart);
            if (Objects.nonNull(index)) {
                List<?> list = getAsList(result);
                if (Objects.nonNull(list)) {
                    result = index < 0 || index >= list.size() ? null : list.get(index);
                    continue;
                }
                int[] intArray = getAsIntegerArray(result);
                if (Objects.nonNull(intArray)) {
                    result = index < 0 || index >= intArray.length ? null : intArray[index];
                    continue;
                }
                byte[] byteArray = getAsByteArray(result);
                if (Objects.nonNull(byteArray)) {
                    result = index < 0 || index >= byteArray.length ? null : byteArray[index];
                    continue;
                }
            }
            result = null;
            break;
        }
        return result;
    }

    public static OptionalInt compare(@Nullable Object value, ConfigurationNode node) {
        if (Objects.isNull(value)) {
            return OptionalInt.empty();
        }
        if (value instanceof Boolean) {
            value = (Boolean) value ? 1 : 0;
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            Integer integerValue = node.getValue(Types::asInt);
            if (Objects.isNull(integerValue)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(integerValue.compareTo(((Number) value).intValue()));
        }
        if (value instanceof Long) {
            Long longValue = node.getValue(Types::asLong);
            if (Objects.isNull(longValue)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(longValue.compareTo((Long) value));
        }
        if (value instanceof Float) {
            Float floatValue = node.getValue(Types::asFloat);
            if (Objects.isNull(floatValue)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(floatValue.compareTo((Float) value));
        }
        if (value instanceof Double) {
            Double doubleValue = node.getValue(Types::asDouble);
            if (Objects.isNull(doubleValue)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(doubleValue.compareTo((Double) value));
        }
        if (value instanceof String) {
            String stringValue = node.getValue(Types::asString);
            if (Objects.isNull(stringValue)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(stringValue.compareTo((String) value));
        }
        return OptionalInt.empty();
    }

    public static boolean isEqual(@Nullable Object value, ConfigurationNode node) {
        if (Objects.isNull(value)) {
            return Objects.isNull(node.getValue());
        }
        if (value instanceof Boolean) {
            value = (Boolean) value ? 1 : 0;
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            Integer integerValue = node.getValue(Types::asInt);
            return Objects.nonNull(integerValue) && integerValue.equals(((Number) value).intValue());
        }
        if (value instanceof Long) {
            Long longValue = node.getValue(Types::asLong);
            return Objects.nonNull(longValue) && longValue.equals(value);
        }
        if (value instanceof Float) {
            Float floatValue = node.getValue(Types::asFloat);
            return Objects.nonNull(floatValue) && floatValue.equals(value);
        }
        if (value instanceof Double) {
            Double doubleValue = node.getValue(Types::asDouble);
            return Objects.nonNull(doubleValue) && doubleValue.equals(value);
        }
        if (value instanceof String) {
            String stringValue = node.getValue(Types::asString);
            return Objects.nonNull(stringValue) && stringValue.equals(value);
        }
        if (node.hasListChildren()) {
            Iterator<? extends ConfigurationNode> iterator = node.getChildrenList().iterator();
            byte[] bytes = getAsByteArray(value);
            if (Objects.nonNull(bytes)) {
                for (byte b : bytes) {
                    if (!iterator.hasNext() || !isEqual(b, iterator.next())) {
                        return false;
                    }
                }
                return !iterator.hasNext();
            }
            int[] ints = getAsIntegerArray(value);
            if (Objects.nonNull(ints)) {
                for (int i : ints) {
                    if (!iterator.hasNext() || !isEqual(i, iterator.next())) {
                        return false;
                    }
                }
                return !iterator.hasNext();
            }
            List<?> list = getAsList(value);
            if (Objects.nonNull(list)) {
                for (Object e : list) {
                    if (!iterator.hasNext() || !isEqual(e, iterator.next())) {
                        return false;
                    }
                }
                return !iterator.hasNext();
            }
            return false;
        }
        if (node.hasMapChildren()) {
            Map<String, ?> map = getAsMap(value);
            Set<? extends Map.Entry<Object, ? extends ConfigurationNode>> entries = node.getChildrenMap().entrySet();
            if (Objects.nonNull(map) && map.size() == entries.size()) {
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : entries) {
                    String key = entry.getKey().toString();
                    if (!map.containsKey(key) || !isEqual(map.get(key), entry.getValue())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Byte getAsByte(@Nullable Object value) {
        return value instanceof Byte ? (Byte) value : value instanceof Boolean ? (Boolean) value ? (byte) 1 : 0 : null;
    }

    @Nullable
    public static Short getAsShort(@Nullable Object value) {
        return value instanceof Short ? (Short) value : null;
    }

    @Nullable
    public static Integer getAsInteger(@Nullable Object value) {
        return value instanceof Integer ? (Integer) value : null;
    }

    @Nullable
    public static Long getAsLong(@Nullable Object value) {
        return value instanceof Long ? (Long) value : null;
    }

    @Nullable
    public static Float getAsFloat(@Nullable Object value) {
        return value instanceof Float ? (Float) value : null;
    }

    @Nullable
    public static Double getAsDouble(@Nullable Object value) {
        return value instanceof Double ? (Double) value : null;
    }

    @Nullable
    public static String getAsString(@Nullable Object value) {
        return value instanceof String ? (String) value : null;
    }

    @Nullable
    public static byte[] getAsByteArray(@Nullable Object value) {
        return value instanceof byte[] ? (byte[]) value : value instanceof Byte[] ? to((Byte[]) value) : null;
    }

    @Nullable
    public static int[] getAsIntegerArray(@Nullable Object value) {
        return value instanceof int[] ? (int[]) value : value instanceof Integer[] ? to((Integer[]) value) : null;
    }

    @Nullable
    public static List<?> getAsList(@Nullable Object value) {
        return value instanceof List ? (List<?>) value : null;
    }

    @Nullable
    public static Map<String, ?> getAsMap(@Nullable Object value) {
        return value instanceof Map ? to((Map<?, ?>) value) : value instanceof DataView ? to((DataView) value) : null;
    }

    @SuppressWarnings("deprecation")
    public static DataContainer toNbt(ItemStack stack) {
        DataContainer container = stack.toContainer();
        DataContainer result = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);

        container.get(DataQuery.of("ItemType")).ifPresent(id -> result.set(DataQuery.of("id"), id));
        container.get(DataQuery.of("UnsafeData")).ifPresent(nbt -> result.set(DataQuery.of("tag"), nbt));
        container.get(DataQuery.of("UnsafeDamage")).ifPresent(damage -> result.set(DataQuery.of("Damage"), damage));

        return result;
    }

    private static byte[] to(Byte[] array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = array[i];
        }
        return bytes;
    }

    private static int[] to(Integer[] array) {
        int[] ints = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            ints[i] = array[i];
        }
        return ints;
    }

    private static Map<String, ?> to(Map<?, ?> map) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            builder.put(entry.getKey().toString(), entry.getValue());
        }
        return builder.build();
    }

    private static Map<String, ?> to(DataView map) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<DataQuery, Object> entry : map.getValues(false).entrySet()) {
            builder.put(entry.getKey().toString(), entry.getValue());
        }
        return builder.build();
    }

    private NbtTypeHelper() {
        // nothing here
    }
}
