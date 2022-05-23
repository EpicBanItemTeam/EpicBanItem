/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.update;

import java.text.MessageFormat;
import java.util.Optional;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonQueryExpression;
import team.ebi.epicbanitem.util.data.DataUtils;

public class PullUpdateExpression implements UpdateExpression {

    private final DataQuery query;
    private final QueryExpression expression;

    public PullUpdateExpression(DataQuery query, QueryExpression expression) {
        this.query = query;
        this.expression = expression;
    }

    public PullUpdateExpression(DataView view, DataQuery query) {
        this.query = DataQuery.of('.', query.last().toString());
        this.expression = new CommonQueryExpression(view, query);
    }

    @Override
    public @NotNull UpdateOperation update(QueryResult result, DataView data) {
        var builder = ImmutableMap.<DataQuery, UpdateOperation>builder();
        for (DataQuery currentQuery : UpdateExpression.parseQuery(query, result)) {
            Optional<Object> currentValue = data.get(currentQuery);
            if (currentValue.isEmpty()) {
                continue;
            }
            DataUtils.operateListOrArray(currentValue.get(), list -> {
                        var finalList = Lists.newArrayList();
                        for (int i = 0; i < list.size(); i++) {
                            var subResult = expression.query(currentQuery.then(String.valueOf(i)), data);
                            if (subResult.isPresent()) {
                                finalList.add(list.get(i));
                            }
                        }
                        return finalList;
                    })
                    .ifPresentOrElse(it -> builder.put(currentQuery, UpdateOperation.replace(currentQuery, it)), () -> {
                        throw new UnsupportedOperationException(
                                MessageFormat.format("$pop failed, {0} is invalid list", currentQuery));
                    });
        }
        return UpdateOperation.common(builder.build());
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew().set(ROOT, expression);
    }
}
