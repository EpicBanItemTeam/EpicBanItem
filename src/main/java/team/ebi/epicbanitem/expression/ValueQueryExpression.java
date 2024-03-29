/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Optional;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.util.Coerce;

import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.query.EqQueryExpression;
import team.ebi.epicbanitem.expression.query.RegexQueryExpression;
import team.ebi.epicbanitem.util.Regex;

public class ValueQueryExpression implements QueryExpression {

    private final QueryExpression expression;

    public ValueQueryExpression(Object value) {
        this.expression = Coerce.asString(value)
                .filter(Regex::isRegex)
                .<QueryExpression>map(RegexQueryExpression::new)
                .orElse(new ArrayableQueryExpression(new EqQueryExpression(value)));
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        return expression.query(query, data);
    }

    @Override
    public DataContainer toContainer() {
        return expression.toContainer();
    }
}
