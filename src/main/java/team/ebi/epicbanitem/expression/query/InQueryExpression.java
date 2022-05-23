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
import org.spongepowered.api.data.persistence.InvalidDataException;

import com.google.common.collect.ImmutableList;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.ValueQueryExpression;

public class InQueryExpression implements QueryExpression {

    private final List<ValueQueryExpression> expressions;

    public InQueryExpression(List<ValueQueryExpression> expressions) {
        this.expressions = expressions;
    }

    @SuppressWarnings("UnstableApiUsage")
    public InQueryExpression(DataView data, DataQuery query) {
        List<?> values = data.getList(query).orElseThrow(() -> new InvalidDataException("$in should be an array"));
        this.expressions = values.stream().map(ValueQueryExpression::new).collect(ImmutableList.toImmutableList());
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        return expressions.stream()
                .map(it -> it.query(query, data))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(ROOT, expressions);
    }
}
