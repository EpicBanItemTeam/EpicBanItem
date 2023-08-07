/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.ImmutableMap;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;
import team.ebi.epicbanitem.util.data.DataUtils;

public class ElemMatchQueryExpression implements QueryExpression {

    private final QueryExpression expression;

    public ElemMatchQueryExpression(QueryExpression expression) {
        this.expression = expression;
    }

    public ElemMatchQueryExpression(DataView view, DataQuery query) {
        this.expression = new CommonQueryExpression(view, query);
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        Optional<List<?>> list = DataUtils.get(data, query).flatMap(DataUtils::asList);
        if (!list.isPresent() || list.get().isEmpty()) {
            return QueryResult.failed();
        }
        List<?> values = list.get();
        ImmutableMap.Builder<String, QueryResult> builder = ImmutableMap.builder();
        boolean matched = false;
        for (int i = 0; i < values.size(); i++) {
            String key = Integer.toString(i);
            Optional<QueryResult> result = this.expression.query(query.then(key), data);
            if (result.isPresent()) {
                builder.put(key, result.get());
                matched = true;
            }
        }
        return QueryResult.fromArray(matched, builder.build());
    }

    @Override
    public DataContainer toContainer() {
        return expression.toContainer();
    }
}
