package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
final class NbtTypeHelper {
    private static final Pattern BOOLEAN;
    private static final Pattern DOUBLE;
    private static final Pattern FLOAT;
    private static final Pattern BYTE;
    private static final Pattern LONG;
    private static final Pattern SHORT;
    private static final Pattern INTEGER;
    private static final Pattern NUMBER;

    private static final Pattern BYTE_ARRAY_HEAD = Pattern.compile("B;\\s*(\\w+)");
    private static final Pattern LONG_ARRAY_HEAD = Pattern.compile("L;\\s*(\\w+)");
    private static final Pattern INTEGER_ARRAY_HEAD = Pattern.compile("I;\\s*(\\w+)");

    // from vanilla
    static {
        BOOLEAN = Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE);
        BYTE = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))b", Pattern.CASE_INSENSITIVE);
        LONG = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))l", Pattern.CASE_INSENSITIVE);
        SHORT = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))s", Pattern.CASE_INSENSITIVE);
        INTEGER = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))", Pattern.CASE_INSENSITIVE);
        NUMBER = Pattern.compile("([-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?)", Pattern.CASE_INSENSITIVE);
        FLOAT = Pattern.compile("([-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?)f", Pattern.CASE_INSENSITIVE);
        DOUBLE = Pattern.compile("([-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?)d", Pattern.CASE_INSENSITIVE);
    }

    private NbtTypeHelper() {
        // nothing here
    }

    static void setObject(DataQuery query, DataView view, Function<Object, ?> valueTransformer) {
        List<String> queryParts = query.getParts();
        int lastQueryPartIndex = queryParts.size() - 1;
        Object[] subViews = new Object[lastQueryPartIndex];
        for (int i = 0; i < lastQueryPartIndex; ++i) {
            Object subView = getObject(queryParts.get(i), i == 0 ? view : subViews[i - 1]);
            subViews[i] = Objects.isNull(subView) ? ImmutableMap.of() : subView;
        }
        for (int i = lastQueryPartIndex; i > 0; --i) {
            Object subView = setObject(queryParts.get(i), subViews[i - 1], valueTransformer);
            valueTransformer = o -> subView;
        }
        DataQuery firstQuery = DataQuery.of(queryParts.get(0));
        Object value = valueTransformer.apply(view.get(firstQuery).orElse(null));
        if (Objects.isNull(value)) {
            view.remove(firstQuery);
        } else {
            view.set(firstQuery, value);
        }
    }

    @Nullable
    static Object getObject(DataQuery query, DataView view) {
        Object subView = view;
        for (String queryPart : query.getParts()) {
            subView = getObject(queryPart, subView);
            if (Objects.isNull(subView)) {
                break;
            }
        }
        return subView;
    }

    /**
     * @param key               key
     * @param view              view
     * @param transformFunction transformFunction
     * @return Object
     * @throws IllegalArgumentException view无法转换成Map且key对应错误的index或无法完成任何解析
     */
    @SuppressWarnings("WeakerAccess")
    static Object setObject(String key, Object view, Function<Object, ?> transformFunction) {
        Map<String, Object> map = getAsMap(view);
        /* make all container mutable */
        if (Objects.nonNull(map)) {
            map = new LinkedHashMap<>(map);
            map.compute(key, (k, v) -> transformFunction.apply(v));
            return map;
        }
        Integer index = Types.asInt(key);
        if (Objects.nonNull(index) && index >= 0) {
            List<Object> list = getAsList(view);
            if (Objects.nonNull(list)) {
                int size = list.size();
                if (index <= size) {
                    if (index == size) {
                        list = new ArrayList<>(list);
                        list.add(transformFunction.apply(size > 0 ? list.get(size - 1) : null));
                        return list;
                    } else {
                        Object newValue = transformFunction.apply(list.get(index));
                        if (Objects.nonNull(newValue)) {
                            list = new ArrayList<>(list);
                            list.set(index, newValue);
                            return list;
                        }
                    }
                }
                throw new IllegalArgumentException("Cannot set value for \"" + key + "\"");
            }
            long[] longArray = getAsLongArray(view);
            if (Objects.nonNull(longArray)) {
                int size = longArray.length;
                if (index <= size) {
                    Object newValue = transformFunction.apply(index == size ? Long.valueOf("0") : longArray[index]);
                    if (newValue instanceof Long) {
                        longArray[index] = (Long) newValue;
                        return longArray;
                    }
                }
                throw new IllegalArgumentException("Cannot set value for \"" + key + "\"");
            }
            int[] intArray = getAsIntegerArray(view);
            if (Objects.nonNull(intArray)) {
                int size = intArray.length;
                if (index <= size) {
                    Object newValue = transformFunction.apply(index == size ? Integer.valueOf("0") : intArray[index]);
                    if (newValue instanceof Integer) {
                        intArray[index] = (Integer) newValue;
                        return intArray;
                    }
                }
                throw new IllegalArgumentException("Cannot set value for \"" + key + "\"");
            }
            byte[] byteArray = getAsByteArray(view);
            if (Objects.nonNull(byteArray)) {
                int size = byteArray.length;
                if (index <= size) {
                    Object newValue = transformFunction.apply(index == size ? Byte.valueOf("0") : byteArray[index]);
                    if (newValue instanceof Byte) {
                        byteArray[index] = (Byte) newValue;
                        return byteArray;
                    }
                }
                throw new IllegalArgumentException("Cannot set value for \"" + key + "\"");
            }
        }
        throw new IllegalArgumentException("Cannot set value for \"" + key + "\"");
    }

    @Nullable
    static Object getObject(String key, Object view) {
        Map<String, ?> map = getAsMap(view);
        if (Objects.nonNull(map)) {
            Object result = map.get(key);
            if (Objects.nonNull(result)) {
                return result;
            }
        }
        Integer index = Types.asInt(key);
        if (Objects.nonNull(index)) {
            List<?> list = getAsList(view);
            if (Objects.nonNull(list)) {
                return index < 0 || index >= list.size() ? null : list.get(index);
            }
            long[] longArray = getAsLongArray(view);
            if (Objects.nonNull(longArray)) {
                return index < 0 || index >= longArray.length ? null : longArray[index];
            }
            int[] intArray = getAsIntegerArray(view);
            if (Objects.nonNull(intArray)) {
                return index < 0 || index >= intArray.length ? null : intArray[index];
            }
            byte[] byteArray = getAsByteArray(view);
            if (Objects.nonNull(byteArray)) {
                return index < 0 || index >= byteArray.length ? null : byteArray[index];
            }
        }
        return null;
    }

    static String toString(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "1b" : "0b";
        }
        if (value instanceof Byte) {
            return value.toString() + "b";
        }
        if (value instanceof Short) {
            return value.toString() + "s";
        }
        if (value instanceof Integer) {
            return value.toString();
        }
        if (value instanceof Long) {
            return value.toString() + "l";
        }
        if (value instanceof Float) {
            return value.toString() + "f";
        }
        if (value instanceof Double) {
            return value.toString() + "d";
        }
        if (value instanceof String) {
            return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"").replace("\u00a7", "\\u00a7") + "\"";
        }
        byte[] bytes = getAsByteArray(value);
        if (Objects.nonNull(bytes)) {
            IntStream range = IntStream.range(0, bytes.length);
            return "[B;" + range.mapToObj(i -> bytes[i] + "b").reduce((a, b) -> a + ", " + b).orElse("") + "]";
        }
        int[] ints = getAsIntegerArray(value);
        if (Objects.nonNull(ints)) {
            IntStream range = IntStream.range(0, ints.length);
            return "[I;" + range.mapToObj(i -> ints[i] + "").reduce((a, b) -> a + ", " + b).orElse("") + "]";
        }
        long[] longs = getAsLongArray(value);
        if (Objects.nonNull(longs)) {
            IntStream range = IntStream.range(0, longs.length);
            return "[L;" + range.mapToObj(i -> longs[i] + "l").reduce((a, b) -> a + ", " + b).orElse("") + "]";
        }
        throw new IllegalArgumentException("The value is a list or a compound");
    }

    static OptionalInt compare(@Nullable Object value, ConfigurationNode node) {
        if (Objects.nonNull(value)) {
            Object another = convert(value, node);
            if (another instanceof String) {
                if (value instanceof String) {
                    return OptionalInt.of(((String) another).compareTo(value.toString()));
                } else {
                    return OptionalInt.empty();
                }
            }
            if (another instanceof Double) {
                if (value instanceof Number) {
                    return OptionalInt.of(Double.compare((Double) another, ((Number) value).doubleValue()));
                } else {
                    return OptionalInt.empty();
                }
            }
            if (another instanceof Long) {
                if (value instanceof Number) {
                    return OptionalInt.of(Long.compare((Long) another, ((Number) value).longValue()));
                } else {
                    return OptionalInt.empty();
                }
            }
            if (another instanceof Float) {
                if (value instanceof Number) {
                    return OptionalInt.of(Float.compare((Float) another, ((Number) value).floatValue()));
                } else {
                    return OptionalInt.empty();
                }
            }
            if (another instanceof Number) {
                if (value instanceof Number) {
                    return OptionalInt.of(Integer.compare(((Number) another).intValue(), ((Number) value).intValue()));
                } else {
                    return OptionalInt.empty();
                }
            }
        }
        return OptionalInt.empty();
    }

    static Object convert(@Nullable Object previous, ConfigurationNode node) {
        if (node.hasListChildren()) {
            return convertAsList(previous, node.getChildrenList());
        }
        if (node.hasMapChildren()) {
            return convertAsObject(previous, node.getChildrenMap());
        }
        Matcher matcher;
        String valueString = node.getString("");
        try {
            matcher = BOOLEAN.matcher(valueString);
            if (matcher.matches()) {
                boolean b = Boolean.parseBoolean(matcher.group(1));
                return previous instanceof Byte ? b ? 1 : 0 : b;
            }
            matcher = DOUBLE.matcher(valueString);
            if (matcher.matches()) {
                return Double.parseDouble(matcher.group(1));
            }
            matcher = FLOAT.matcher(valueString);
            if (matcher.matches()) {
                return Float.parseFloat(matcher.group(1));
            }
            matcher = BYTE.matcher(valueString);
            if (matcher.matches()) {
                byte b = Byte.parseByte(matcher.group(1));
                return previous instanceof Boolean ? b == 0 ? Boolean.FALSE : b == 1 ? Boolean.TRUE : b : b;
            }
            matcher = LONG.matcher(valueString);
            if (matcher.matches()) {
                return Long.parseLong(matcher.group(1));
            }
            matcher = SHORT.matcher(valueString);
            if (matcher.matches()) {
                return Short.parseShort(matcher.group(1));
            }
            matcher = INTEGER.matcher(valueString);
            if (matcher.matches()) {
                int i = Integer.parseInt(matcher.group(1));
                if (previous instanceof Byte && (byte) i == i) {
                    return (byte) i;
                }
                if (previous instanceof Short && (short) i == i) {
                    return (short) i;
                }
                if (previous instanceof Long) {
                    return (long) i;
                }
                if (previous instanceof Float) {
                    return (float) i;
                }
                if (previous instanceof Double) {
                    return (double) i;
                }
                return i;
            }
            matcher = NUMBER.matcher(valueString);
            if (matcher.matches()) {
                double n = Double.parseDouble(matcher.group(1));
                return previous instanceof Float ? (float) n : n;
            }
            return valueString;
        } catch (NumberFormatException e) {
            return valueString;
        }
    }

    private static Map<String, Object> convertAsObject(@Nullable Object previous, Map<Object, ? extends ConfigurationNode> map) {
        Map<String, Object> previousMap = getAsMap(previous);
        if (Objects.isNull(previousMap)) {
            previousMap = Collections.emptyMap();
        }
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            builder.put(key, convert(previousMap.get(key), entry.getValue()));
        }
        return builder.build();
    }

    private static Object convertAsList(@Nullable Object previous, List<? extends ConfigurationNode> list) {
        Object array = convertAsArray(previous, list);
        if (Objects.isNull(array)) {
            int size = list.size();
            List<Object> previousList = getAsList(previous);
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            int previousSize = Objects.isNull(previousList) ? 0 : previousList.size();
            for (int i = 0; i < size; ++i) {
                ConfigurationNode node = list.get(i);
                builder.add(convert(i >= previousSize ? null : previousList.get(i), node));
            }
            return builder.build();
        }
        return array;
    }

    @Nullable
    private static Object convertAsArray(@Nullable Object previous, List<? extends ConfigurationNode> list) {
        if (Objects.isNull(getAsList(previous)) && !list.isEmpty()) {
            Matcher matcher;
            ConfigurationNode node = list.get(0);
            String headString = node.getString("");
            try {
                matcher = BYTE_ARRAY_HEAD.matcher(headString);
                if (matcher.matches()) {
                    int size = list.size();
                    byte[] bytes = new byte[size];
                    Byte previousElement = Byte.valueOf("0");
                    for (int i = 0; i < size; ++i) {
                        node = i > 0 ? list.get(i) : node.copy().setValue(matcher.group(1));
                        Byte element = getAsByte(convert(previousElement, node));
                        bytes[i] = Objects.requireNonNull(element);
                    }
                    return bytes;
                }
                matcher = INTEGER_ARRAY_HEAD.matcher(headString);
                if (matcher.matches()) {
                    int size = list.size();
                    int[] ints = new int[size];
                    Integer previousElement = Integer.valueOf("0");
                    for (int i = 0; i < size; ++i) {
                        node = i > 0 ? list.get(i) : node.copy().setValue(matcher.group(1));
                        Integer element = getAsInteger(convert(previousElement, node));
                        ints[i] = Objects.requireNonNull(element);
                    }
                    return ints;
                }
                matcher = LONG_ARRAY_HEAD.matcher(headString);
                if (matcher.matches()) {
                    int size = list.size();
                    long[] longs = new long[size];
                    Long previousElement = Long.valueOf("0");
                    for (int i = 0; i < size; ++i) {
                        node = i > 0 ? list.get(i) : node.copy().setValue(matcher.group(1));
                        Long element = getAsLong(convert(previousElement, node));
                        longs[i] = Objects.requireNonNull(element);
                    }
                    return longs;
                }
            } catch (NullPointerException ignored) {
                // fall through
            }
        }
        return null;
    }

    static boolean isEqual(@Nullable Object value, Object another) {
        Map<String, Object> valueMap = getAsMap(value);
        if (Objects.nonNull(valueMap)) {
            Map<String, Object> anotherMap = getAsMap(another);
            if (Objects.isNull(anotherMap) || anotherMap.size() != valueMap.size()) {
                return false;
            }
            for (Map.Entry<String, Object> entry : anotherMap.entrySet()) {
                if (!isEqual(valueMap.get(entry.getKey()), entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        List<Object> valueList = getAsList(value);
        if (Objects.nonNull(valueList)) {
            List<Object> anotherList = getAsList(another);
            if (Objects.isNull(anotherList) || anotherList.size() != valueList.size()) {
                return false;
            }
            for (int i = anotherList.size() - 1; i >= 0; --i) {
                if (!isEqual(anotherList.get(i), valueList.get(i))) {
                    return false;
                }
            }
            return true;
        }
        byte[] valueBytes = getAsByteArray(value);
        if (Objects.nonNull(valueBytes)) {
            return Arrays.equals(valueBytes, getAsByteArray(another));
        }
        int[] valueInts = getAsIntegerArray(value);
        if (Objects.nonNull(valueInts)) {
            return Arrays.equals(valueInts, getAsIntegerArray(another));
        }
        long[] valueLongs = getAsLongArray(value);
        if (Objects.nonNull(valueLongs)) {
            return Arrays.equals(valueLongs, getAsLongArray(another));
        }
        return another.equals(value);
    }

    @Nullable
    static Byte getAsByte(@Nullable Object value) {
        return value instanceof Byte ? (Byte) value : value instanceof Boolean ? (Boolean) value ? (byte) 1 : 0 : null;
    }

    @Nullable
    static Short getAsShort(@Nullable Object value) {
        return value instanceof Short ? (Short) value : null;
    }

    @Nullable
    static Integer getAsInteger(@Nullable Object value) {
        return value instanceof Integer ? (Integer) value : null;
    }

    @Nullable
    static Long getAsLong(@Nullable Object value) {
        return value instanceof Long ? (Long) value : null;
    }

    @Nullable
    static Float getAsFloat(@Nullable Object value) {
        return value instanceof Float ? (Float) value : null;
    }

    @Nullable
    static Double getAsDouble(@Nullable Object value) {
        return value instanceof Double ? (Double) value : null;
    }

    @Nullable
    static String getAsString(@Nullable Object value) {
        return value instanceof String ? (String) value : null;
    }

    @Nullable
    static byte[] getAsByteArray(@Nullable Object value) {
        return value instanceof byte[] ? (byte[]) value : value instanceof Byte[] ? to((Byte[]) value) : null;
    }

    @Nullable
    static int[] getAsIntegerArray(@Nullable Object value) {
        return value instanceof int[] ? (int[]) value : value instanceof Integer[] ? to((Integer[]) value) : null;
    }

    @Nullable
    static long[] getAsLongArray(@Nullable Object value) {
        return value instanceof long[] ? (long[]) value : value instanceof Long[] ? to((Long[]) value) : null;
    }

    @Nullable
    static List<Object> getAsList(@Nullable Object value) {
        return value instanceof List ? ImmutableList.copyOf((List<?>) value) : null;
    }

    @Nullable
    static Map<String, Object> getAsMap(@Nullable Object value) {
        return value instanceof Map ? to((Map<?, ?>) value) : value instanceof DataView ? to((DataView) value) : null;
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

    private static long[] to(Long[] array) {
        long[] longs = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            longs[i] = array[i];
        }
        return longs;
    }

    private static Map<String, Object> to(Map<?, ?> map) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            builder.put(entry.getKey().toString(), entry.getValue());
        }
        return builder.build();
    }

    private static Map<String, Object> to(DataView map) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<DataQuery, Object> entry : map.getValues(false).entrySet()) {
            builder.put(entry.getKey().toString(), entry.getValue());
        }
        return builder.build();
    }
}
