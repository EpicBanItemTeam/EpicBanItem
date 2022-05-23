/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class OrQueryExpression implements QueryExpression {

    private final Set<QueryExpression> expressions;

    public OrQueryExpression(Set<QueryExpression> expressions) {
        this.expressions = expressions;
    }

    public OrQueryExpression(DataView data, DataQuery query) {
        List<DataView> views =
                data.getViewList(query).orElseThrow(() -> new InvalidDataException("$or should be objects array"));
        this.expressions = views.stream()
                .map(it -> new CommonQueryExpression(it, it.currentPath()))
                .collect(Collectors.toSet());
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
