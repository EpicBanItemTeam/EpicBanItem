/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Map;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.Maps;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunctions;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.update.SetUpdateExpression;

import static team.ebi.epicbanitem.api.expression.ExpressionQueries.UPDATE_EXPRESSIONS;

public class CommonUpdateExpression implements UpdateExpression {

    private final Map<DataQuery, UpdateExpression> expressions;

    public CommonUpdateExpression(DataView view, DataQuery query) {
        this.expressions = Maps.newHashMap();
        for (DataQuery subQuery : view.keys(false)) {
            String key = subQuery.toString();
            DataQuery entireQuery = query.then(subQuery);
            if (UPDATE_EXPRESSIONS.contains(subQuery)) {
                this.expressions.put(
                        entireQuery,
                        UpdateExpressionFunctions.expressions.get(key).apply(view, entireQuery));
            } else {
                this.expressions.put(ROOT, new ObjectUpdateExpression(SetUpdateExpression::new, view, query));
            }
        }
    }

    @Override
    public UpdateOperation update(QueryResult result, DataView data) {
        UpdateOperation operation = UpdateOperation.common();
        for (UpdateExpression expression : expressions.values()) {
            operation = operation.merge(expression.update(result, data));
        }
        return operation;
    }

    @Override
    public DataContainer toContainer() {
        final var container = DataContainer.createNew();
        this.expressions.forEach((query, expression) -> {
            final var targetContainer = expression.toContainer();
            if (query.equals(ROOT)) {
                for (final var key : targetContainer.keys(false)) {
                    container.set(key, targetContainer.get(key).orElseThrow());
                }
            } else if (!query.parts().isEmpty()) {
                container.set(query, targetContainer.get(ROOT).orElse(targetContainer));
            }
        });
        return container;
    }
}
