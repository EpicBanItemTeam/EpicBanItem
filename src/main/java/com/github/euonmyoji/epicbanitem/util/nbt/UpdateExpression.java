package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class UpdateExpression implements DataTransformer {
    private static final Map<String, BiFunction<String, ConfigurationNode, DataTransformer>> operators;

    static {
        ImmutableMap.Builder<String, BiFunction<String, ConfigurationNode, DataTransformer>> builder;
        builder = ImmutableMap.builder();

        builder.put("$unset", (k, n) -> new Unset(k));
        builder.put("$rename", (k, n) -> new Rename(k, n.getString(k)));

        builder.put("$set", (k, n) -> new Transform((q, v) -> o -> NbtTypeHelper.convert(o, n), k));
        builder.put("$inc", (k, n) -> new Transform((q, v) -> o -> increaseValue(o, NbtTypeHelper.convert(o, n)), k));
        builder.put("$mul", (k, n) -> new Transform((q, v) -> o -> multiplyValue(o, NbtTypeHelper.convert(o, n)), k));

        builder.put("$pop", (k, n) -> new Transform((q, v) -> o -> popValue(o, n.getInt()), k));
        builder.put("$pull", (k, n) -> new Transform((q, v) -> arrayFilterTransformer(pullFilter(q, v, n)), k));
        builder.put("$pullAll", (k, n) -> new Transform((q, v) -> arrayFilterTransformer(pullAllFilter(q, v, n)), k));

        operators = builder.build();
    }

    private final List<DataTransformer> transformers;

    public UpdateExpression(ConfigurationNode view) {
        Map<Object, ? extends ConfigurationNode> map = view.getChildrenMap();
        this.transformers = new ArrayList<>(map.size());
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            BiFunction<String, ConfigurationNode, DataTransformer> operator = operators.get(key);
            if (Objects.nonNull(operator)) {
                entry.getValue().getChildrenMap().forEach((k, v) -> {
                    DataTransformer transformer = operator.apply(k.toString(), v);
                    this.transformers.add(transformer);
                });
                continue;
            }
            this.transformers.clear();
            break;
        }
        if (this.transformers.isEmpty()) {
            this.transformers.add(new Replace(map));
        }
    }

    private static UpdateResult getUpdateResult(DataQuery query, UpdateResult.Operation operation) {
        for (String part : Lists.reverse(query.getParts())) {
            operation = UpdateResult.Operation.update(UpdateResult.update(ImmutableMap.of(part, operation)));
        }
        // noinspection ConstantConditions
        return operation.getUpdateResult().get();
    }

    private static List<DataQuery> expandQuery(DataQuery previous, QueryResult result) {
        List<String> previousParts = previous.getParts();
        Stream<Tuple<ImmutableList<String>, Optional<QueryResult>>> parts;
        parts = Stream.of(new Tuple<>(ImmutableList.of(), Optional.of(result)));
        for (String previousPart : previousParts) {
            switch (previousPart) {
                case "$":
                    parts = parts.flatMap(tuple -> {
                        ImmutableList<String> part = tuple.getFirst();
                        Optional<QueryResult> partResult = tuple.getSecond();
                        if (!partResult.isPresent() || !partResult.get().isArrayChildren()) {
                            String message = "Cannot match \"$\" in \"" + String.join(".", part) + ".$\" because its parent is not an array";
                            throw new IllegalArgumentException(message);
                        }
                        Map<String, QueryResult> queryMap = partResult.get().getChildren();
                        if (!queryMap.isEmpty()) {
                            ImmutableList.Builder<String> b = ImmutableList.builder();
                            Map.Entry<String, QueryResult> e = queryMap.entrySet().iterator().next();
                            return Stream.of(Tuple.of(b.addAll(part).add(e.getKey()).build(), Optional.of(e.getValue())));
                        }
                        return Stream.empty();
                    });
                    break;
                case "$[]":
                    parts = parts.flatMap(tuple -> {
                        ImmutableList<String> part = tuple.getFirst();
                        Optional<QueryResult> partResult = tuple.getSecond();
                        if (!partResult.isPresent() || !partResult.get().isArrayChildren()) {
                            String message = "Cannot match \"$[]\" in \"" + String.join(".", part) + ".$[]\" because its parent is not an array";
                            throw new IllegalArgumentException(message);
                        }
                        Map<String, QueryResult> queryMap = partResult.get().getChildren();
                        return queryMap.entrySet().stream().map(e -> {
                            ImmutableList.Builder<String> b = ImmutableList.builder();
                            return Tuple.of(b.addAll(part).add(e.getKey()).build(), Optional.of(e.getValue()));
                        });
                    });
                    break;
                default:
                    parts = parts.map(tuple -> {
                        ImmutableList<String> part = tuple.getFirst();
                        Optional<QueryResult> partResult = tuple.getSecond();
                        partResult = partResult.flatMap(v -> Optional.ofNullable(v.getChildren().get(previousPart)));
                        return Tuple.of(ImmutableList.<String>builder().addAll(part).add(previousPart).build(), partResult);
                    });
            }
        }
        return parts.map(tuple -> DataQuery.of(tuple.getFirst())).collect(ImmutableList.toImmutableList());
    }

    private static Function<Object, ?> arrayFilterTransformer(IntPredicate predicate) {
        return o -> {
            List<Object> list = NbtTypeHelper.getAsList(o);
            if (Objects.nonNull(list)) {
                Collector<Object, ?, ImmutableList<Object>> collector = ImmutableList.toImmutableList();
                return IntStream.range(0, list.size()).filter(predicate).mapToObj(list::get).collect(collector);
            }
            long[] longArray = NbtTypeHelper.getAsLongArray(o);
            if (Objects.nonNull(longArray)) {
                return IntStream.range(0, longArray.length).filter(predicate).mapToObj(i -> longArray[i]).toArray(Long[]::new);
            }
            int[] intArray = NbtTypeHelper.getAsIntegerArray(o);
            if (Objects.nonNull(intArray)) {
                return IntStream.range(0, intArray.length).filter(predicate).mapToObj(i -> intArray[i]).toArray(Integer[]::new);
            }
            byte[] byteArray = NbtTypeHelper.getAsByteArray(o);
            if (Objects.nonNull(byteArray)) {
                return IntStream.range(0, byteArray.length).filter(predicate).mapToObj(i -> byteArray[i]).toArray(Byte[]::new);
            }
            throw new IllegalArgumentException("Cannot apply filters to a non-array value");
        };
    }

    private static IntPredicate pullFilter(DataQuery query, DataView view, ConfigurationNode node) {
        return node.hasMapChildren() ? i -> {
            QueryExpression queryExpression = new QueryExpression(node);
            return !queryExpression.query(query.then(Integer.toString(i)), view).isPresent();
        } : i -> {
            Object value = NbtTypeHelper.getObject(query.then(Integer.toString(i)), view);
            return !NbtTypeHelper.isEqual(value, NbtTypeHelper.convert(value, node));
        };
    }

    private static IntPredicate pullAllFilter(DataQuery query, DataView view, ConfigurationNode node) {
        return i -> node.getChildrenList().stream().noneMatch(n -> {
            Object value = NbtTypeHelper.getObject(query.then(Integer.toString(i)), view);
            return NbtTypeHelper.isEqual(value, NbtTypeHelper.convert(value, n));
        });
    }

    private static Object multiplyValue(Object previousValue, Object multiplier) {
        if (Objects.isNull(previousValue)) {
            return multiplyValue(multiplier, 0);
        }
        if (multiplier instanceof Boolean) {
            multiplier = (Boolean) multiplier ? (byte) 1 : (byte) 0;
        }
        if (!(multiplier instanceof Number)) {
            throw new IllegalArgumentException("Cannot multiply with non-numeric argument: " + multiplier);
        }
        if (previousValue instanceof Boolean) {
            return ((Boolean) previousValue ? (byte) 1 : (byte) 0) * ((Number) multiplier).byteValue();
        }
        if (previousValue instanceof Byte) {
            return (Byte) previousValue * ((Number) multiplier).byteValue();
        }
        if (previousValue instanceof Short) {
            return (Short) previousValue * ((Number) multiplier).shortValue();
        }
        if (previousValue instanceof Integer) {
            return (Integer) previousValue * ((Number) multiplier).intValue();
        }
        if (previousValue instanceof Long) {
            return (Long) previousValue * ((Number) multiplier).longValue();
        }
        if (previousValue instanceof Float) {
            return (Float) previousValue * ((Number) multiplier).floatValue();
        }
        if (previousValue instanceof Double) {
            return (Double) previousValue * ((Number) multiplier).doubleValue();
        }
        throw new IllegalArgumentException("Cannot apply $mul to a value of non-numeric type");
    }

    private static Object increaseValue(Object previousValue, Object increment) {
        if (Objects.isNull(previousValue)) {
            return increment;
        }
        if (increment instanceof Boolean) {
            increment = (Boolean) increment ? (byte) 1 : (byte) 0;
        }
        if (!(increment instanceof Number)) {
            throw new IllegalArgumentException("Cannot increase with non-numeric argument: " + increment);
        }
        if (previousValue instanceof Boolean) {
            return ((Boolean) previousValue ? (byte) 1 : (byte) 0) + ((Number) increment).byteValue();
        }
        if (previousValue instanceof Byte) {
            return (Byte) previousValue + ((Number) increment).byteValue();
        }
        if (previousValue instanceof Short) {
            return (Short) previousValue + ((Number) increment).shortValue();
        }
        if (previousValue instanceof Integer) {
            return (Integer) previousValue + ((Number) increment).intValue();
        }
        if (previousValue instanceof Long) {
            return (Long) previousValue + ((Number) increment).longValue();
        }
        if (previousValue instanceof Float) {
            return (Float) previousValue + ((Number) increment).floatValue();
        }
        if (previousValue instanceof Double) {
            return (Double) previousValue + ((Number) increment).doubleValue();
        }
        throw new IllegalArgumentException("Cannot apply $inc to a value of non-numeric type");
    }

    private static Object popValue(Object previousValue, int index) {
        List<Object> list = NbtTypeHelper.getAsList(previousValue);
        if (Objects.nonNull(list)) {
            if (index == 1) {
                return list.subList(1, list.size());
            }
            if (index == -1) {
                return list.subList(0, list.size() - 1);
            }
            throw new IllegalArgumentException("$pop expects 1 or -1, found: " + index);
        }
        long[] longArray = NbtTypeHelper.getAsLongArray(previousValue);
        if (Objects.nonNull(longArray)) {
            if (index == 1) {
                return Arrays.copyOfRange(longArray, 1, longArray.length);
            }
            if (index == -1) {
                return Arrays.copyOfRange(longArray, 0, longArray.length - 1);
            }
            throw new IllegalArgumentException("$pop expects 1 or -1, found: " + index);
        }
        int[] intArray = NbtTypeHelper.getAsIntegerArray(previousValue);
        if (Objects.nonNull(intArray)) {
            if (index == 1) {
                return Arrays.copyOfRange(intArray, 1, intArray.length);
            }
            if (index == -1) {
                return Arrays.copyOfRange(intArray, 0, intArray.length - 1);
            }
            throw new IllegalArgumentException("$pop expects 1 or -1, found: " + index);
        }
        byte[] byteArray = NbtTypeHelper.getAsByteArray(previousValue);
        if (Objects.nonNull(byteArray)) {
            if (index == 1) {
                return Arrays.copyOfRange(byteArray, 1, byteArray.length);
            }
            if (index == -1) {
                return Arrays.copyOfRange(byteArray, 0, byteArray.length - 1);
            }
            throw new IllegalArgumentException("$pop expects 1 or -1, found: " + index);
        }
        return previousValue;
    }

    @Override
    public UpdateResult update(QueryResult result, DataView view) {
        UpdateResult updateResult = UpdateResult.nothing();
        for (DataTransformer transformer : this.transformers) {
            updateResult = transformer.update(result, view).merge(updateResult.getChildren());
        }
        return updateResult;
    }

    private static class Transform implements DataTransformer {
        private final BiFunction<DataQuery, DataView, Function<Object, ?>> valueTransformer;
        private final DataQuery queryFirst;
        private final DataQuery query;

        private Transform(BiFunction<DataQuery, DataView, Function<Object, ?>> valueTransformer, String key) {
            this.valueTransformer = valueTransformer;
            this.query = DataQuery.of('.', key);
            this.queryFirst = DataQuery.of(this.query.getParts().get(0));
        }

        @Override
        public UpdateResult update(QueryResult result, DataView view) {
            DataContainer copy = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
            List<DataQuery> transformedQueries = expandQuery(this.query, result);
            view.get(this.queryFirst).ifPresent(v -> copy.set(this.queryFirst, v));
            for (DataQuery query : transformedQueries) {
                NbtTypeHelper.setObject(query, copy, this.valueTransformer.apply(query, copy));
            }
            UpdateResult updateResult = UpdateResult.nothing();
            for (DataQuery query : transformedQueries) {
                Object object = Objects.requireNonNull(NbtTypeHelper.getObject(query, copy));
                UpdateResult.Operation operation = UpdateResult.Operation.replace(object);
                updateResult = getUpdateResult(query, operation).merge(updateResult.getChildren());
            }
            return updateResult;
        }
    }

    private static class Unset implements DataTransformer {
        private final DataQuery query;

        private Unset(String key) {
            this.query = DataQuery.of('.', key);
        }

        @Override
        public UpdateResult update(QueryResult result, DataView view) {
            List<DataQuery> transformedQueries = expandQuery(this.query, result);
            UpdateResult updateResult = UpdateResult.nothing();
            for (DataQuery query : transformedQueries) {
                UpdateResult.Operation operation = UpdateResult.Operation.remove();
                updateResult = getUpdateResult(query, operation).merge(updateResult.getChildren());
            }
            return updateResult;
        }
    }

    private static class Rename implements DataTransformer {
        private final DataQuery sourceQuery;
        private final DataQuery targetQuery;

        private Rename(String key, String target) {
            this.sourceQuery = DataQuery.of('.', key);
            this.targetQuery = DataQuery.of('.', target);
        }

        @Override
        public UpdateResult update(QueryResult result, DataView view) {
            if (this.sourceQuery.equals(this.targetQuery)) {
                String source = this.sourceQuery.toString(), target = this.targetQuery.toString();
                String s = "The query source \"" + source + "\" and target \"" + target + "\" should differ";
                throw new IllegalArgumentException(s);
            }
            List<DataQuery> transformedTarget = expandQuery(this.targetQuery, result);
            if (transformedTarget.size() != 1 || !transformedTarget.get(0).equals(this.targetQuery)) {
                String target = this.targetQuery.toString();
                String s = "The query destination \"" + target + "\" should not be dynamic";
                throw new IllegalArgumentException(s);
            }
            List<DataQuery> transformedSource = expandQuery(this.sourceQuery, result);
            if (transformedSource.size() != 1 || !transformedSource.get(0).equals(this.sourceQuery)) {
                String source = this.sourceQuery.toString();
                String s = "The query source \"" + source + "\" should not be dynamic";
                throw new IllegalArgumentException(s);
            }
            Object data = NbtTypeHelper.getObject(this.sourceQuery, view);
            if (Objects.isNull(data)) {
                String source = this.sourceQuery.toString();
                throw new IllegalArgumentException("The query source \"" + source + "\" should contain value");
            }
            UpdateResult replaceUpdate = getUpdateResult(this.targetQuery, UpdateResult.Operation.replace(data));
            UpdateResult removeUpdate = getUpdateResult(this.sourceQuery, UpdateResult.Operation.remove());
            return removeUpdate.merge(replaceUpdate.getChildren());
        }
    }

    private static class Replace implements DataTransformer {
        private final Map<String, ConfigurationNode> value;

        private Replace(Map<Object, ? extends ConfigurationNode> nodes) {
            ImmutableMap.Builder<String, ConfigurationNode> builder = ImmutableMap.builder();
            nodes.forEach((k, v) -> builder.put(k.toString(), v));
            this.value = builder.build();
        }

        @Override
        public UpdateResult update(QueryResult result, DataView view) {
            ImmutableMap.Builder<String, UpdateResult.Operation> builder = ImmutableMap.builder();
            for (Map.Entry<DataQuery, Object> entry : view.getValues(false).entrySet()) {
                String key = entry.getKey().toString();
                if (!this.value.containsKey(key)) {
                    builder.put(key, UpdateResult.Operation.remove());
                }
            }
            for (Map.Entry<String, ConfigurationNode> entry : this.value.entrySet()) {
                String key = entry.getKey();
                Object previous = NbtTypeHelper.getObject(key, view);
                builder.put(key, UpdateResult.Operation.replace(NbtTypeHelper.convert(previous, entry.getValue())));
            }
            return UpdateResult.update(builder.build());
        }
    }
}
