/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.QueryResult.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryResultTest {

    @Test
    void merge() {
        var result = QueryResult.success(Map.of("0", QueryResult.success()));
        var result2 = QueryResult.success(Map.of("foo", QueryResult.success()));
        assertEquals(result, result.merge(QueryResult.success()));
        QueryResult merged = result.merge(result2);
        assertEquals(2, merged.size());
        assertEquals(Set.of("0", "foo"), merged.keySet());
    }

    @Test
    void type() {
        assertEquals(Type.ARRAY, Type.ARRAY.merge(Type.DEFAULT));
        assertEquals(Type.ARRAY, Type.DEFAULT.merge(Type.ARRAY));
        assertEquals(Type.DEFAULT, Type.DEFAULT.merge(Type.DEFAULT));
    }
}
