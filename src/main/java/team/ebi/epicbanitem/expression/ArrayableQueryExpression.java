/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.base.Suppliers;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.query.ElemMatchQueryExpression;

/**
 * Use for wrap the expressions that can test for value and array
 */
public class ArrayableQueryExpression implements QueryExpression {

    private final Supplier<ElemMatchQueryExpression> elemMatchExpression;
    private final QueryExpression expression;

    public ArrayableQueryExpression(QueryExpression expression) {
        this.elemMatchExpression = Suppliers.memoize(() -> new ElemMatchQueryExpression(expression));
        this.expression = expression;
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        Optional<QueryResult> result = this.expression.query(query, data);
        return result.isPresent() ? result : this.elemMatchExpression.get().query(query, data);
    }

    @Override
    public DataContainer toContainer() {
        return expression.toContainer();
    }
}
