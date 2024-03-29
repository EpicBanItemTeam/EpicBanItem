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

import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.data.DataUtils;

public class SizeQueryExpression implements QueryExpression {

    private final int size;

    public SizeQueryExpression(int size) {
        this.size = size;
    }

    public SizeQueryExpression(DataView data, DataQuery query) {
        this(data.getInt(query).orElseThrow(() -> new InvalidDataException("$size should be int")));
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        int currentSize = DataUtils.get(data, query)
                .flatMap(DataUtils::asList)
                .map(List::size)
                .orElse(-1);
        return QueryResult.from(currentSize == size);
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(ROOT, size);
    }
}
