/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.expression;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.expression.SimpleQueryResult;

public interface QueryResult extends Map<String, QueryResult> {

    static Optional<QueryResult> failed() {
        return Optional.empty();
    }

    static QueryResult success(Map<String, QueryResult> children) {
        return new SimpleQueryResult(Type.DEFAULT, children);
    }

    static QueryResult array(Map<String, QueryResult> children) {
        return new SimpleQueryResult(Type.ARRAY, children);
    }

    static QueryResult success() {
        return new SimpleQueryResult();
    }

    static Optional<QueryResult> from(boolean b) {
        return b ? Optional.of(success()) : failed();
    }

    static Optional<QueryResult> from(boolean b, Map<String, QueryResult> children) {
        return b ? Optional.of(success(children)) : failed();
    }

    static Optional<QueryResult> fromArray(boolean b, Map<String, QueryResult> children) {
        return b ? Optional.of(array(children)) : failed();
    }

    @Contract("_ -> new")
    QueryResult merge(@NotNull QueryResult target);

    QueryResult.Type type();

    enum Type {
        DEFAULT,
        ARRAY;

        public Type merge(Type type) {
            return this == DEFAULT && type == DEFAULT ? DEFAULT : ARRAY;
        }
    }
}
