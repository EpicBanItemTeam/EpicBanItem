package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ustc_zzzz
 */
public class QueryExpression implements DataPredicate {
    private static final Map<String, Function<ConfigurationNode, DataPredicate>> operators;

    static {
        ImmutableMap.Builder<String, Function<ConfigurationNode, DataPredicate>> builder = ImmutableMap.builder();

        builder.put("$or", n -> new Or(n.getChildrenList(), QueryExpression::new, true));
        builder.put("$nor", n -> new Nor(n.getChildrenList(), QueryExpression::new, true));
        builder.put("$and", n -> new And(n.getChildrenList(), QueryExpression::new, true));

        builder.put("$not", n -> new Not(n, true));

        builder.put("$eq", n -> new OneOrMore(new Eq(n)));
        builder.put("$ne", n -> new OneOrMore(new Ne(n)));
        builder.put("$gt", n -> new OneOrMore(new Compare(n, i -> i < 0)));
        builder.put("$lt", n -> new OneOrMore(new Compare(n, i -> i > 0)));
        builder.put("$gte", n -> new OneOrMore(new Compare(n, i -> i <= 0)));
        builder.put("$lte", n -> new OneOrMore(new Compare(n, i -> i >= 0)));
        builder.put("$in", n -> new OneOrMore(new Or(n.getChildrenList(), QueryExpression::getInPredicate, false)));
        builder.put("$nin", n -> new OneOrMore(new Nor(n.getChildrenList(), QueryExpression::getInPredicate, false)));

        // noinspection Convert2MethodRef
        builder.put("$exists", n -> new Exists(n));
        builder.put("$tagType", node -> new OneOrMore(new TagType(node)));

        operators = builder.build();
    }

    private final List<DataPredicate> criteria;

    public QueryExpression(ConfigurationNode view) {
        Map<Object, ? extends ConfigurationNode> map = view.getChildrenMap();
        this.criteria = new ArrayList<>(map.size());
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            if ("$and".equals(key) || "$nor".equals(key) || "$or".equals(key)) {
                this.criteria.add(operators.get(key).apply(entry.getValue()));
                continue;
            }
            if ("$where".equals(key)) {
                // TODO
                continue;
            }
            ConfigurationNode value = entry.getValue();
            String valueString = value.getString("");
            int regexpEnd = getRegexpEnd(valueString);
            if (regexpEnd < 0) {
                List<DataPredicate> collectedCriteria = findOperators(value);
                if (collectedCriteria.isEmpty()) {
                    this.criteria.add(new WithPrefix(key, new OneOrMore(new Eq(value))));
                    continue;
                }
                for (DataPredicate criterion : collectedCriteria) {
                    this.criteria.add(new WithPrefix(key, criterion));
                }
                continue;
            }
            this.criteria.add(new WithPrefix(key, new Regexp(valueString, regexpEnd, "")));
        }
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView view) {
        Map<String, QueryResult> map = ImmutableMap.of();
        for (DataPredicate criterion : this.criteria) {
            Optional<QueryResult> result = criterion.query(query, view);
            if (result.isPresent()) {
                map = result.get().merge(map).getChildren();
            } else {
                return QueryResult.failure();
            }
        }
        return QueryResult.successObject(map);
    }

    private static ImmutableList<DataPredicate> findOperators(ConfigurationNode node) {
        ImmutableList.Builder<DataPredicate> builder = ImmutableList.builder();
        if (node.hasMapChildren()) {
            Map<Object, ? extends ConfigurationNode> map = node.getChildrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                if ("$regex".equals(key)) {
                    String options = node.getNode("$options").getString("");
                    String regexp = entry.getValue().getString("");
                    int regexpEnd = getRegexpEnd(regexp);
                    if (regexpEnd < 0) {
                        regexpEnd = regexp.length() + 1;
                        regexp = '/' + regexp + '/';
                    }
                    builder.add(new Regexp(regexp, regexpEnd, options));
                    continue;
                }
                Function<ConfigurationNode, DataPredicate> operator = operators.get(key);
                if (Objects.nonNull(operator)) {
                    builder.add(operator.apply(entry.getValue()));
                    continue;
                }
                return ImmutableList.of();
            }
        }
        return builder.build();
    }

    private static DataPredicate getInPredicate(ConfigurationNode node) {
        String string = node.getString("");
        int regexpEnd = getRegexpEnd(string);
        return regexpEnd < 0 ? new Eq(node) : new Regexp(string, regexpEnd, "");
    }

    private static int getRegexpEnd(String value) {
        int lastIndex = value.lastIndexOf('/');
        return lastIndex > 0 && value.indexOf('/') == 0 ? lastIndex : -1;
    }

    private static class WithPrefix implements DataPredicate {
        private final DataPredicate criterion;
        private final String prefix;

        private WithPrefix(String prefix, DataPredicate criterion) {
            this.criterion = criterion;
            this.prefix = prefix;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            DataQuery then = DataQuery.of('.', this.prefix);
            Optional<QueryResult> result = this.criterion.query(query.then(then), view);
            if (result.isPresent()) {
                for (String part : then.getParts()) {
                    // noinspection ConstantConditions
                    result = QueryResult.successObject(ImmutableMap.of(part, result.get()));
                }
                return result;
            }
            return Optional.empty();
        }
    }

    private static class OneOrMore implements DataPredicate {
        private final DataPredicate criterion;

        private OneOrMore(DataPredicate criterion) {
            this.criterion = criterion;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            Optional<QueryResult> result = this.criterion.query(query, view);
            if (result.isPresent()) {
                return result;
            }
            List<?> list = NbtTypeHelper.getAsList(NbtTypeHelper.getObject(query, view));
            if (Objects.nonNull(list)) {
                ImmutableMap.Builder<String, QueryResult> builder = ImmutableMap.builder();
                for (int i = 0; i < list.size(); i++) {
                    result = this.criterion.query(query.then(Integer.toString(i)), view);
                    if (result.isPresent()) {
                        builder.put(Integer.toString(i), result.get());
                    }
                }
                return QueryResult.successArray(builder.build());
            }
            return QueryResult.failure();
        }
    }

    private static class Eq implements DataPredicate {
        private final ConfigurationNode node;

        private Eq(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            return QueryResult.check(NbtTypeHelper.isEqual(NbtTypeHelper.getObject(query, view), this.node));
        }
    }

    private static class Ne implements DataPredicate {
        private final ConfigurationNode node;

        private Ne(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            return QueryResult.check(!NbtTypeHelper.isEqual(NbtTypeHelper.getObject(query, view), this.node));
        }
    }

    private static class Compare implements DataPredicate {
        private final ConfigurationNode node;
        private final IntPredicate predicate;

        private Compare(ConfigurationNode node, IntPredicate predicate) {
            this.predicate = predicate;
            this.node = node;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            OptionalInt optionalInt = NbtTypeHelper.compare(NbtTypeHelper.getObject(query, view), this.node);
            return QueryResult.check(optionalInt.isPresent() && this.predicate.test(optionalInt.getAsInt()));
        }
    }

    private static class Not implements DataPredicate {
        private final List<DataPredicate> criteria;

        private Not(ConfigurationNode node, boolean checkEmpty) {
            ImmutableList<DataPredicate> operators = findOperators(node);
            Preconditions.checkArgument(!checkEmpty || !operators.isEmpty());
            this.criteria = operators;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            for (DataPredicate criterion : this.criteria) {
                Optional<QueryResult> result = criterion.query(query, view);
                if (!result.isPresent()) {
                    return QueryResult.success();
                }
            }
            return QueryResult.failure();
        }
    }

    private static class And implements DataPredicate {
        private final List<DataPredicate> criteria;

        private <T> And(List<? extends T> views, Function<T, ? extends DataPredicate> f, boolean checkEmpty) {
            Preconditions.checkArgument(!checkEmpty || !views.isEmpty());
            this.criteria = views.stream().map(f).collect(Collectors.toList());
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            boolean isArray = true, isObject = true;
            Map<String, QueryResult> queryResultMap = ImmutableMap.of();
            for (DataPredicate criterion : this.criteria) {
                Optional<QueryResult> resultOptional = criterion.query(query, view);
                if (resultOptional.isPresent()) {
                    QueryResult result = resultOptional.get();
                    isArray = isArray && result.isArrayChildren();
                    isObject = isObject && result.isObjectChildren();
                    queryResultMap = result.merge(queryResultMap).getChildren();
                } else {
                    return QueryResult.failure();
                }
            }
            if (isArray) {
                return QueryResult.successArray(queryResultMap);
            }
            if (isObject) {
                return QueryResult.successObject(queryResultMap);
            }
            return QueryResult.success();
        }
    }

    private static class Or implements DataPredicate {
        private final List<DataPredicate> criteria;

        private <T> Or(List<? extends T> views, Function<T, ? extends DataPredicate> f, boolean checkEmpty) {
            Preconditions.checkArgument(!checkEmpty || !views.isEmpty());
            this.criteria = views.stream().map(f).collect(Collectors.toList());
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            for (DataPredicate criterion : this.criteria) {
                Optional<QueryResult> result = criterion.query(query, view);
                if (result.isPresent()) {
                    return result;
                }
            }
            return QueryResult.failure();
        }
    }

    private static class Nor implements DataPredicate {
        private final List<DataPredicate> criteria;

        private <T> Nor(List<? extends T> views, Function<T, ? extends DataPredicate> f, boolean checkEmpty) {
            Preconditions.checkArgument(!checkEmpty || !views.isEmpty());
            this.criteria = views.stream().map(f).collect(Collectors.toList());
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            for (DataPredicate criterion : this.criteria) {
                Optional<QueryResult> result = criterion.query(query, view);
                if (result.isPresent()) {
                    return QueryResult.failure();
                }
            }
            return QueryResult.success();
        }
    }

    private static class Regexp implements DataPredicate {
        private final Pattern pattern;

        private Regexp(String regexp, int endIndex, String options) {
            int flag = 0;
            for (int i = regexp.length() - 1; i > endIndex; --i) {
                flag = this.addFlag(flag, regexp.charAt(i));
            }
            for (int i = options.length() - 1; i >= 0; --i) {
                flag = this.addFlag(flag, regexp.charAt(i));
            }
            // noinspection MagicConstant
            this.pattern = Pattern.compile(regexp.substring(1, endIndex), flag);
        }

        private int addFlag(int flag, char option) {
            switch (option) {
                case 'i':
                    return flag | Pattern.CASE_INSENSITIVE;
                case 'm':
                    return flag | Pattern.MULTILINE;
                case 'x':
                    return flag | Pattern.COMMENTS;
                case 's':
                    return flag | Pattern.DOTALL;
                default:
                    return flag;
            }
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            String string = NbtTypeHelper.getAsString(NbtTypeHelper.getObject(query, view));
            return QueryResult.check(Optional.ofNullable(string).filter(this.pattern.asPredicate()).isPresent());
        }
    }

    private static class Exists implements DataPredicate {
        private final boolean existentialState;

        private Exists(ConfigurationNode node) {
            Integer i = node.getValue(Types::asInt);
            if (Objects.nonNull(i)) {
                Preconditions.checkArgument(i == 0 || i == 1);
                this.existentialState = i == 1;
            } else {
                Boolean b = node.getValue(Types::strictAsBoolean);
                this.existentialState = Preconditions.checkNotNull(b);
            }
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            boolean exists = Objects.nonNull(NbtTypeHelper.getObject(query, view));
            return QueryResult.check(this.existentialState == exists);
        }
    }

    private static class TagType implements DataPredicate {
        private static final List<Predicate<Object>> matchers = getMatchers();
        private static final List<Tuple<String, Integer>> matcherAliases = getMatcherAliases();

        private final int[] types;

        private TagType(ConfigurationNode node) {
            Stream<Integer> stream = nodeToList(node).stream().map(n -> n.getValue(TagType::asInt));
            this.types = stream.mapToInt(t -> Math.max(0, Math.min(13, Preconditions.checkNotNull(t)))).toArray();
        }

        private static Integer asInt(Object value) {
            Integer integer = Types.asInt(value);
            if (Objects.isNull(integer)) {
                String string = String.valueOf(value);
                for (Tuple<String, Integer> matcherAlias : matcherAliases) {
                    if (string.equalsIgnoreCase(matcherAlias.getFirst())) {
                        integer = matcherAlias.getSecond();
                        break;
                    }
                }
            }
            return integer;
        }

        private static List<? extends ConfigurationNode> nodeToList(ConfigurationNode node) {
            return node.hasListChildren() ? node.getChildrenList() : Collections.singletonList(node);
        }

        private static ImmutableList<Predicate<Object>> getMatchers() {
            ImmutableList.Builder<Predicate<Object>> builder = ImmutableList.builder();

            builder.add(value -> false); // 0
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsByte(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsShort(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsInteger(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsLong(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsFloat(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsDouble(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsByteArray(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsString(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsList(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsMap(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsIntegerArray(value)));
            builder.add(value -> Objects.nonNull(NbtTypeHelper.getAsLongArray(value)));
            builder.add(value -> false); // 13

            return builder.build();
        }

        private static List<Tuple<String, Integer>> getMatcherAliases() {
            ImmutableList.Builder<Tuple<String, Integer>> builder = ImmutableList.builder();

            builder.add(new Tuple<>("byte", 1));
            builder.add(new Tuple<>("bytearray", 7));
            builder.add(new Tuple<>("byte_array", 7));
            builder.add(new Tuple<>("compound", 10));
            builder.add(new Tuple<>("double", 6));
            builder.add(new Tuple<>("end", 0));
            builder.add(new Tuple<>("float", 5));
            builder.add(new Tuple<>("int", 3));
            builder.add(new Tuple<>("intarray", 11));
            builder.add(new Tuple<>("int_array", 11));
            builder.add(new Tuple<>("list", 9));
            builder.add(new Tuple<>("long", 4));
            builder.add(new Tuple<>("longarray", 12));
            builder.add(new Tuple<>("long_array", 12));
            builder.add(new Tuple<>("short", 2));
            builder.add(new Tuple<>("string", 8));

            return builder.build();
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            for (int type : this.types) {
                if (matchers.get(type).test(NbtTypeHelper.getObject(query, view))) {
                    return QueryResult.success();
                }
            }
            return QueryResult.failure();
        }
    }
}
