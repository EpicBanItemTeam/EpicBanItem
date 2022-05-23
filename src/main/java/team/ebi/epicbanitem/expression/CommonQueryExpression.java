/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Tuple;

import com.google.common.collect.Maps;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunctions;
import team.ebi.epicbanitem.api.expression.QueryResult;

import static team.ebi.epicbanitem.api.expression.ExpressionQueries.ROOT_QUERY_EXPRESSIONS;

public record CommonQueryExpression(Map<DataQuery, QueryExpression> expressions) implements QueryExpression {

    public CommonQueryExpression(DataView view) {
        this(view, DataQuery.of());
    }

    public CommonQueryExpression(DataView view, DataQuery query) {
        this(Maps.newHashMap());
        view.getView(query).ifPresent(expressionView -> expressionView.keys(false).stream()
                .map(it -> Tuple.of(it, expressionView.get(it).orElseThrow()))
                .forEach(tuple -> {
                    // Can be root expressions or nbt path
                    DataQuery key = tuple.first();
                    Object value = tuple.second();
                    DataQuery entireQuery = query.then(key);
                    if (ROOT_QUERY_EXPRESSIONS.contains(key)) {
                        this.expressions.put(
                                key,
                                QueryExpressionFunctions.expressions
                                        .get(key.toString())
                                        .apply(view, entireQuery));
                    } else if (value instanceof DataView valueView) {
                        // Not the root. Children should be node paths.
                        for (DataQuery subKey : valueView.keys(false)) {
                            entireQuery = entireQuery.then(subKey);
                            String stringSubKey = subKey.toString();
                            // Have to be an expression. Not support nested nbt path.
                            if (QueryExpressionFunctions.expressions.containsKey(stringSubKey)) {
                                this.expressions.put(
                                        key.then(subKey),
                                        new ExtraQueryQueryExpression(
                                                QueryExpressionFunctions.expressions
                                                        .get(stringSubKey)
                                                        .apply(view, entireQuery),
                                                DataQuery.of('.', key.toString())));
                            }
                        }
                    } else {
                        // Not view or root. Should be nbt path with a value.
                        this.expressions.put(
                                key,
                                new ExtraQueryQueryExpression(
                                        new ValueQueryExpression(view.get(entireQuery)
                                                .orElseThrow(() -> new InvalidDataException("No value in view"))),
                                        DataQuery.of('.', key.toString())));
                    }
                }));
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        QueryResult result = QueryResult.success();
        for (QueryExpression expression : this.expressions.values()) {
            Optional<QueryResult> currentResult = expression.query(query, data);
            if (currentResult.isPresent()) {
                result = currentResult.get().merge(result);
            } else {
                return QueryResult.failed();
            }
        }
        return Optional.of(result);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = DataContainer.createNew();
        this.expressions.forEach((query, expression) ->
                container.set(query, expression.toContainer().get(ROOT).orElse(expression.toContainer())));
        return container;
    }
}
