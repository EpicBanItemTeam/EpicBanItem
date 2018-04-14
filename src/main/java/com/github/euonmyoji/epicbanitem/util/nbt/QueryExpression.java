package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
        Map<String, DataPredicate> map = new HashMap<>();
        for (DataPredicate criterion : this.criteria) {
            Optional<Map<String, DataPredicate>> result = criterion.testAndGetArrayPlaceholderFinder(query, view);
            if (result.isPresent()) {
                map.putAll(result.get());
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(map);
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
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            query = query.then(DataQuery.of('.', this.prefix));
            Optional<Map<String, DataPredicate>> result = this.criterion.testAndGetArrayPlaceholderFinder(query, view);
            if (result.isPresent()) {
                ImmutableMap.Builder<String, DataPredicate> builder = ImmutableMap.builder();
                for (Map.Entry<String, DataPredicate> entry : result.get().entrySet()) {
                    builder.put(this.prefix + '.' + entry.getKey(), entry.getValue());
                }
                return Optional.of(builder.build());
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
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            Optional<Map<String, DataPredicate>> result = this.criterion.testAndGetArrayPlaceholderFinder(query, view);
            if (result.isPresent()) {
                return Optional.of(Collections.emptyMap());
            }
            List<?> list = NbtTypeHelper.getAsList(NbtTypeHelper.getObject(query, view));
            if (Objects.nonNull(list)) {
                for (int i = 0; i < list.size(); i++) {
                    result = this.criterion.testAndGetArrayPlaceholderFinder(query.then(Integer.toString(i)), view);
                    if (result.isPresent()) {
                        return Optional.of(Collections.singletonMap("", this.criterion));
                    }
                }
            }
            return Optional.empty();
        }
    }

    private static class Eq implements DataPredicate {
        private final ConfigurationNode node;

        private Eq(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            boolean isEqual = NbtTypeHelper.isEqual(NbtTypeHelper.getObject(query, view), this.node);
            return isEqual ? Optional.of(Collections.emptyMap()) : Optional.empty();
        }
    }

    private static class Ne implements DataPredicate {
        private final ConfigurationNode node;

        private Ne(ConfigurationNode node) {
            this.node = node;
        }

        @Override
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            boolean isEqual = NbtTypeHelper.isEqual(NbtTypeHelper.getObject(query, view), this.node);
            return isEqual ? Optional.empty() : Optional.of(Collections.emptyMap());
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
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            OptionalInt optionalInt = NbtTypeHelper.compare(NbtTypeHelper.getObject(query, view), this.node);
            boolean match = optionalInt.isPresent() && this.predicate.test(optionalInt.getAsInt());
            return match ? Optional.of(Collections.emptyMap()) : Optional.empty();
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
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            for (DataPredicate criterion : this.criteria) {
                Optional<Map<String, DataPredicate>> result = criterion.testAndGetArrayPlaceholderFinder(query, view);
                if (!result.isPresent()) {
                    return Optional.of(Collections.emptyMap());
                }
            }
            return Optional.empty();
        }
    }

    private static class And implements DataPredicate {
        private final List<DataPredicate> criteria;

        private <T> And(List<? extends T> views, Function<T, ? extends DataPredicate> f, boolean checkEmpty) {
            Preconditions.checkArgument(!checkEmpty || !views.isEmpty());
            this.criteria = views.stream().map(f).collect(Collectors.toList());
        }

        @Override
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            Map<String, DataPredicate> map = new HashMap<>();
            for (DataPredicate criterion : this.criteria) {
                Optional<Map<String, DataPredicate>> result = criterion.testAndGetArrayPlaceholderFinder(query, view);
                if (result.isPresent()) {
                    map.putAll(result.get());
                } else {
                    return Optional.empty();
                }
            }
            return Optional.of(map);
        }
    }

    private static class Or implements DataPredicate {
        private final List<DataPredicate> criteria;

        private <T> Or(List<? extends T> views, Function<T, ? extends DataPredicate> f, boolean checkEmpty) {
            Preconditions.checkArgument(!checkEmpty || !views.isEmpty());
            this.criteria = views.stream().map(f).collect(Collectors.toList());
        }

        @Override
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            for (DataPredicate criterion : this.criteria) {
                Optional<Map<String, DataPredicate>> result = criterion.testAndGetArrayPlaceholderFinder(query, view);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        }
    }

    private static class Nor implements DataPredicate {
        private final List<DataPredicate> criteria;

        private <T> Nor(List<? extends T> views, Function<T, ? extends DataPredicate> f, boolean checkEmpty) {
            Preconditions.checkArgument(!checkEmpty || !views.isEmpty());
            this.criteria = views.stream().map(f).collect(Collectors.toList());
        }

        @Override
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            for (DataPredicate criterion : this.criteria) {
                Optional<Map<String, DataPredicate>> result = criterion.testAndGetArrayPlaceholderFinder(query, view);
                if (result.isPresent()) {
                    return Optional.empty();
                }
            }
            return Optional.of(Collections.emptyMap());
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
        public Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view) {
            String string = NbtTypeHelper.getAsString(NbtTypeHelper.getObject(query, view));
            return Optional.ofNullable(string).filter(this.pattern.asPredicate()).map(s -> Collections.emptyMap());
        }
    }
}
