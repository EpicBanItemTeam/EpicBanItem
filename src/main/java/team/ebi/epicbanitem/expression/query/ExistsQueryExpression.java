/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Coerce;

import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.data.DataUtils;

public class ExistsQueryExpression implements QueryExpression {

    private static final String EXCEPTION = "$exists should be one of 0, 1, true or false. Current: {0}";
    private final boolean expect;

    public ExistsQueryExpression(boolean expect) {
        this.expect = expect;
    }

    public ExistsQueryExpression(DataView data, DataQuery query) {
        expect = data.get(query)
                .map(value -> Coerce.asInteger(value)
                        .filter(Set.of(0, 1)::contains)
                        .map(it -> it == 1)
                        .or(() -> Coerce.asBoolean(value))
                        .orElseThrow(() -> new InvalidDataException(MessageFormat.format(EXCEPTION, value))))
                .orElseThrow(() -> new InvalidDataException(MessageFormat.format(EXCEPTION, "null")));
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        return QueryResult.from(DataUtils.get(data, query).isPresent() == expect);
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(ROOT, expect);
    }
}
