/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.util.Optional;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NorQueryExpression implements QueryExpression {

    private final QueryExpression expression;

    public NorQueryExpression(OrQueryExpression expression) {
        this.expression = expression;
    }

    public NorQueryExpression(DataView data, DataQuery query) {
        if (!data.getViewList(query).isPresent()) {
            throw new InvalidDataException("$nor should be an object array");
        }
        this.expression = new OrQueryExpression(data, query);
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        return QueryResult.from(!expression.query(query, data).isPresent());
    }

    @Override
    public DataContainer toContainer() {
        return expression.toContainer();
    }
}
