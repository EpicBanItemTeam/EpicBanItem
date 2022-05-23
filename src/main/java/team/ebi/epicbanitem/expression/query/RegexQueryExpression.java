/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Coerce;

import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.Regex;
import team.ebi.epicbanitem.util.data.DataUtils;

public class RegexQueryExpression implements QueryExpression {

    private final Pattern pattern;
    private final Regex regex;

    public RegexQueryExpression(String value) {
        this.regex = new Regex(value);
        this.pattern = regex.pattern();
    }

    public RegexQueryExpression(DataView view, DataQuery query) {
        this(view.getString(query).orElseThrow(() -> new InvalidDataException("$regex should be string")));
    }

    @Override
    public Optional<QueryResult> query(DataQuery query, DataView data) {
        return QueryResult.from(DataUtils.get(data, query)
                .flatMap(Coerce::asString)
                .filter(this.pattern.asPredicate())
                .isPresent());
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(ROOT, regex.toString());
    }
}
