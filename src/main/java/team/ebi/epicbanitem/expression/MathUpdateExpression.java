/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Optional;
import java.util.function.BinaryOperator;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class MathUpdateExpression implements UpdateExpression {

    private final DataQuery query;
    private final Number argNumber;
    private final BinaryOperator<Number> operator;

    public MathUpdateExpression(DataQuery query, Number argNumber, BinaryOperator<Number> operator) {
        this.query = query;
        this.argNumber = argNumber;
        this.operator = operator;
    }

    public MathUpdateExpression(DataView view, DataQuery query, BinaryOperator<Number> operator) {
        this.query = DataQuery.of('.', query.last().toString());
        this.argNumber = view.get(query)
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .orElseThrow(() -> new InvalidDataException(query + "need a number input"));
        this.operator = operator;
    }

    @Override
    public @NotNull UpdateOperation update(QueryResult result, DataView data) {
        ImmutableMap.Builder<DataQuery, UpdateOperation> builder = ImmutableMap.builder();
        for (DataQuery currentQuery : UpdateExpression.parseQuery(query, result)) {
            Optional<Number> value = data.get(currentQuery).map(it -> {
                if (it instanceof Number n) {
                    return n;
                }
                return null;
            });
            if (value.isEmpty()) {
                continue;
            }
            builder.put(
                    currentQuery, UpdateOperation.replace(currentQuery, this.operator.apply(value.get(), argNumber)));
        }

        return UpdateOperation.common(builder.build());
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(ROOT, argNumber);
    }
}
