/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.update;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.util.data.DataUtils;

public class PopUpdateExpression implements UpdateExpression {

    private final DataQuery query;
    private final Position value;

    public PopUpdateExpression(DataQuery query, Position value) {
        this.query = query;
        this.value = value;
    }

    public PopUpdateExpression(DataView view, DataQuery query) {
        this.query = DataQuery.of('.', query.last().toString());
        this.value = Position.fromId(view.getInt(query)
                .filter(it -> Math.abs(it) == 1)
                .orElseThrow(() -> new InvalidDataException(query + "need 1 or -1")));
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
                        if (value == Position.FIRST) {
                            list.remove(0);
                        } else {
                            list.remove(list.size() - 1);
                        }
                        return list;
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
        return DataContainer.createNew().set(ROOT, value);
    }

    enum Position {
        FIRST(-1),
        LAST(1);

        private static final ImmutableMap<Integer, Position> BY_ID = ImmutableMap.<Integer, Position>builder()
                .putAll(Arrays.stream(Position.values())
                        .collect(Collectors.<Position, Integer, Position>toMap(it -> it.id, Function.identity())))
                .build();

        public final int id;

        Position(int id) {
            this.id = id;
        }

        static Position fromId(int id) {
            return BY_ID.get(id);
        }
    }
}
