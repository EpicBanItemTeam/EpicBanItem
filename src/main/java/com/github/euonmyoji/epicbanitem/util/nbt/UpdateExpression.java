package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ustc_zzzz
 */
public class UpdateExpression implements DataTransformer {
    private static final Map<String, BiFunction<String, ConfigurationNode, DataTransformer>> operators;

    static {
        ImmutableMap.Builder<String, BiFunction<String, ConfigurationNode, DataTransformer>> builder;
        builder = ImmutableMap.builder();

        builder.put("$unset", (k, n) -> new Unset(k));
        builder.put("$rename", (k, n) -> new Rename(k, n.getString(k)));

        builder.put("$set", (k, n) -> new Transform(o -> NbtTypeHelper.convert(o, n), k));
        builder.put("$inc", (k, n) -> new Transform(o -> increaseValue(o, NbtTypeHelper.convert(o, n)), k));
        builder.put("$mul", (k, n) -> new Transform(o -> multiplyValue(o, NbtTypeHelper.convert(o, n)), k));

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

    @Override
    public UpdateResult update(QueryResult result, DataView view) {
        UpdateResult updateResult = UpdateResult.nothing();
        for (DataTransformer transformer : this.transformers) {
            updateResult = transformer.update(result, view).merge(updateResult.getChildren());
        }
        return updateResult;
    }

    private static UpdateResult getUpdateResult(DataQuery query, UpdateResult.Operation operation) {
        for (String part : Lists.reverse(query.getParts())) {
            operation = UpdateResult.Operation.update(UpdateResult.update(ImmutableMap.of(part, operation)));
        }
        // noinspection ConstantConditions
        return operation.getUpdateResult().get();
    }

    private static List<DataQuery> transformQuery(DataQuery previous, QueryResult result) {
        List<String> previousParts = previous.getParts();
        List<Tuple<ArrayList<String>, QueryResult>> parts;
        parts = Collections.singletonList(new Tuple<>(new ArrayList<>(previousParts.size()), result));
        for (String previousPart : previousParts) {
            switch (previousPart) {
                case "$":
                    parts = parts.stream().map(tuple -> {
                        QueryResult partResult = tuple.getSecond();
                        if (!partResult.isArrayChildren()) {
                            String prefix = String.join(".", tuple.getFirst().get(0));
                            String s = "Cannot match \"$\" in \"" + prefix + ".$\" because its parent is not an array";
                            throw new IllegalArgumentException(s);
                        }
                        Map<String, QueryResult> queryMap = partResult.getChildren();
                        if (queryMap.isEmpty()) {
                            String prefix = String.join(".", tuple.getFirst().get(0));
                            String s = "Cannot match \"$\" in \"" + prefix + ".$\" because no element matches";
                            throw new IllegalArgumentException(s);
                        }
                        ArrayList<String> partKey = tuple.getFirst();
                        partKey.add(queryMap.keySet().iterator().next());
                        return Tuple.of(partKey, queryMap.values().iterator().next());
                    }).collect(Collectors.toList());
                    break;
                case "$[]":
                    parts = parts.stream().flatMap(tuple -> {
                        QueryResult partResult = tuple.getSecond();
                        if (!partResult.isArrayChildren()) {
                            String prefix = String.join(".", tuple.getFirst().get(0));
                            String s = "Cannot match \"$\" in \"" + prefix + ".$\" because its parent is not an array";
                            throw new IllegalArgumentException(s);
                        }
                        Map<String, QueryResult> queryMap = partResult.getChildren();
                        if (queryMap.isEmpty()) {
                            String prefix = String.join(".", tuple.getFirst().get(0));
                            String s = "Cannot match \"$\" in \"" + prefix + ".$\" because no element matches";
                            throw new IllegalArgumentException(s);
                        }
                        return queryMap.entrySet().stream().map(e -> {
                            ArrayList<String> partKey = tuple.getFirst();
                            partKey.add(e.getKey());
                            return Tuple.of(partKey, e.getValue());
                        });
                    }).collect(Collectors.toList());
                    break;
                default:
                    parts = parts.stream().map(tuple -> {
                        ArrayList<String> partKey = tuple.getFirst();
                        partKey.add(previousPart);
                        return Tuple.of(partKey, tuple.getSecond());
                    }).collect(Collectors.toList());
            }
        }
        return parts.stream().map(tuple -> DataQuery.of(tuple.getFirst())).collect(Collectors.toList());
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

    private static class Transform implements DataTransformer {
        private final Function<Object, Object> valueTransformer;
        private final DataQuery queryFirst;
        private final DataQuery query;

        private Transform(Function<Object, ?> valueTransformer, String key) {
            this.valueTransformer = valueTransformer::apply;
            this.query = DataQuery.of('.', key);
            this.queryFirst = DataQuery.of(this.query.getParts().get(0));
        }

        @Override
        public UpdateResult update(QueryResult result, DataView view) {
            @SuppressWarnings("deprecation")
            DataContainer copy = new MemoryDataContainer(DataView.SafetyMode.ALL_DATA_CLONED);
            List<DataQuery> transformedQueries = transformQuery(this.query, result);
            view.get(this.queryFirst).ifPresent(v -> copy.set(this.queryFirst, v));
            for (DataQuery query : transformedQueries) {
                NbtTypeHelper.setObject(query, copy, this.valueTransformer);
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
            List<DataQuery> transformedQueries = transformQuery(this.query, result);
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
            List<DataQuery> transformedTarget = transformQuery(this.targetQuery, result);
            if (transformedTarget.size() != 1 || !transformedTarget.get(0).equals(this.targetQuery)) {
                String target = this.targetQuery.toString();
                String s = "The query destination \"" + target + "\" should not be dynamic";
                throw new IllegalArgumentException(s);
            }
            List<DataQuery> transformedSource = transformQuery(this.sourceQuery, result);
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
