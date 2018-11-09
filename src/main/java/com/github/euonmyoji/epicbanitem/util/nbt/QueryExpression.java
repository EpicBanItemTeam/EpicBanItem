package com.github.euonmyoji.epicbanitem.util.nbt;

import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.Tuple;

import javax.script.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
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
        builder.put("$size", n -> new Size(n));
        builder.put("$all", n -> new AllIn(n.getChildrenList()));
        builder.put("$elemMatch", n -> new ElemMatch(new QueryExpression(n)));

        // noinspection Convert2MethodRef
        builder.put("$exists", n -> new Exists(n));
        builder.put("$tagType", n -> new OneOrMore(new TagType(n)));

        operators = builder.build();
    }

    private final List<DataPredicate> criteria;

    public QueryExpression(ConfigurationNode view) {
        Map<Object, ? extends ConfigurationNode> map = view.getChildrenMap();
        this.criteria = new ArrayList<>(map.size());
        DataPredicate whereExpression = null;
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            if ("$and".equals(key) || "$nor".equals(key) || "$or".equals(key)) {
                this.criteria.add(operators.get(key).apply(entry.getValue()));
                continue;
            }
            if ("$where".equals(key)) {
                whereExpression = new Where(entry.getValue());
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
        if (Objects.nonNull(whereExpression)) {
            this.criteria.add(whereExpression);
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
            String regexp = "", options = "";
            Map<Object, ? extends ConfigurationNode> map = node.getChildrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                if ("$regex".equals(key)) {
                    regexp = entry.getValue().getString("");
                    continue;
                }
                if ("$options".equals(key)) {
                    options = entry.getValue().getString("");
                    continue;
                }
                if (operators.containsKey(key)) {
                    builder.add(operators.get(key).apply(entry.getValue()));
                    continue;
                }
                return ImmutableList.of();
            }
            if (!regexp.isEmpty()) {
                int regexpEnd = getRegexpEnd(regexp);
                if (regexpEnd < 0) {
                    regexpEnd = regexp.length() + 1;
                    regexp = '/' + regexp + '/';
                }
                builder.add(new Regexp(regexp, regexpEnd, options));
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

    private static int getListCount(Object object) {
        List<?> list = NbtTypeHelper.getAsList(object);
        if (Objects.nonNull(list)) {
            return list.size();
        }
        long[] longArray = NbtTypeHelper.getAsLongArray(object);
        if (Objects.nonNull(longArray)) {
            return longArray.length;
        }
        int[] intArray = NbtTypeHelper.getAsIntegerArray(object);
        if (Objects.nonNull(intArray)) {
            return intArray.length;
        }
        byte[] byteArray = NbtTypeHelper.getAsByteArray(object);
        if (Objects.nonNull(byteArray)) {
            return byteArray.length;
        }
        return -1;
    }

    private static class WithPrefix implements DataPredicate {
        private final DataPredicate criterion;
        private final String prefix;

        private WithPrefix(String prefix, DataPredicate criterion) {
            int index = prefix.indexOf('.');
            if (index < 0) {
                this.criterion = criterion;
                this.prefix = prefix;
            } else {
                this.criterion = new OneOrMore(new WithPrefix(prefix.substring(index + 1), criterion));
                this.prefix = prefix.substring(0, index);
            }
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            Optional<QueryResult> resultOptional = this.criterion.query(query.then(this.prefix), view);
            return resultOptional.flatMap(result -> QueryResult.successObject(ImmutableMap.of(this.prefix, result)));
        }
    }

    private static class OneOrMore implements DataPredicate {
        private final ElemMatch elemMatchCriterion;
        private final DataPredicate criterion;

        private OneOrMore(DataPredicate criterion) {
            this.elemMatchCriterion = new ElemMatch(criterion);
            this.criterion = criterion;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            Optional<QueryResult> result = this.criterion.query(query, view);
            return result.isPresent() ? result : this.elemMatchCriterion.query(query, view);
        }
    }

    private static class Eq implements DataPredicate {
        private final ConfigurationNode node;

        private Eq(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            Object value = NbtTypeHelper.getObject(query, view);
            return QueryResult.check(NbtTypeHelper.isEqual(value, NbtTypeHelper.convert(value, this.node)));
        }
    }

    private static class Ne implements DataPredicate {
        private final ConfigurationNode node;

        private Ne(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            Object value = NbtTypeHelper.getObject(query, view);
            return QueryResult.check(!NbtTypeHelper.isEqual(value, NbtTypeHelper.convert(value, this.node)));
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
                flag = this.addFlag(flag, options.charAt(i));
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

    private static class AllIn implements DataPredicate {
        private final List<DataPredicate> criteria;

        private AllIn(List<? extends ConfigurationNode> nodes) {
            ImmutableList.Builder<DataPredicate> builder = ImmutableList.builder();
            for (ConfigurationNode node : nodes) {
                ConfigurationNode elemMatch = node.getChildrenMap().get("$elemMatch");
                if (Objects.nonNull(elemMatch)) {
                    builder.add(new QueryExpression(elemMatch));
                } else {
                    builder.add(getInPredicate(node));
                }
            }
            this.criteria = builder.build();
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            int size = getListCount(NbtTypeHelper.getObject(query, view));
            if (size > 0) {
                boolean matchedInArray = false;
                ImmutableMap.Builder<String, QueryResult> builder = ImmutableMap.builder();
                for (int i = 0; i < size; i++) {
                    String key = Integer.toString(i);
                    DataQuery queryThen = query.then(key);
                    for (DataPredicate criterion : this.criteria) {
                        Optional<QueryResult> result = criterion.query(queryThen, view);
                        if (result.isPresent()) {
                            builder.put(key, result.get());
                            matchedInArray = true;
                            break;
                        }
                    }
                }
                if (matchedInArray) {
                    return QueryResult.successArray(builder.build());
                }
            }
            return QueryResult.failure();
        }
    }

    private static class Size implements DataPredicate {
        private final int size;

        private Size(ConfigurationNode node) {
            Object converted = NbtTypeHelper.convert(0, node);
            this.size = converted instanceof Integer ? (Integer) converted : -1;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            int size = getListCount(NbtTypeHelper.getObject(query, view));
            return QueryResult.check(size >= 0 && size == this.size);
        }
    }

    private static class ElemMatch implements DataPredicate {
        private final DataPredicate criterion;

        private ElemMatch(DataPredicate criterion) {
            this.criterion = criterion;
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            int size = getListCount(NbtTypeHelper.getObject(query, view));
            if (size > 0) {
                boolean matchedInArray = false;
                ImmutableMap.Builder<String, QueryResult> builder = ImmutableMap.builder();
                for (int i = 0; i < size; i++) {
                    String key = Integer.toString(i);
                    Optional<QueryResult> result = this.criterion.query(query.then(key), view);
                    if (result.isPresent()) {
                        builder.put(key, result.get());
                        matchedInArray = true;
                    }
                }
                if (matchedInArray) {
                    return QueryResult.successArray(builder.build());
                }
            }
            return QueryResult.failure();
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

    private static class Where implements DataPredicate {
        private static final ScriptEngine ENGINE = getJavaScriptEngine();

        private final CompiledScript compiledScript;
        private final Bindings bindings = new SimpleBindings();

        private Where(ConfigurationNode node) {
            try {
                this.compiledScript = ((Compilable) ENGINE).compile(wrap(node.getString("")));
            } catch (ScriptException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private static String wrap(String script) {
            try {
                // test if it is an expression
                ((Compilable) ENGINE).compile(script);
                return "!(function(){return typeof(this)=='function'?this.apply(obj):this;}).apply((function(){return (" + script + ");}).apply(obj))";
            } catch (ScriptException e) {
                return "!(new Function(" + TextUtil.escape(script) + ").apply(obj))";
            }
        }

        private static ScriptEngine getJavaScriptEngine() {
            // noinspection SpellCheckingInspection
            return new ScriptEngineManager(null).getEngineByName("nashorn");
        }

        private static Map<String, Object> transform(Map<String, Object> map) {
            return Maps.transformValues(map, v -> {
                List<Object> l = NbtTypeHelper.getAsList(v);
                if (Objects.nonNull(l)) {
                    return l.toArray(new Object[0]);
                }
                Map<String, Object> m = NbtTypeHelper.getAsMap(v);
                if (Objects.nonNull(m)) {
                    return transform(m);
                }
                return v;
            });
        }

        @Override
        public Optional<QueryResult> query(DataQuery query, DataView view) {
            try {
                this.bindings.clear();
                this.bindings.put("obj", transform(NbtTypeHelper.getAsMap(view)));
                return QueryResult.check(Boolean.FALSE.equals(this.compiledScript.eval(this.bindings)));
            } catch (ScriptException e) {
                return QueryResult.failure();
            }
        }
    }
}
