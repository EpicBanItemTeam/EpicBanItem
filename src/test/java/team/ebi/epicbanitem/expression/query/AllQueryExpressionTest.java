/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Set;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult.Type;
import team.ebi.epicbanitem.expression.ValueQueryExpression;

import static org.junit.jupiter.api.Assertions.*;

class AllQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(
                DataQuery.of("foo"),
                List.of(
                        new DummyDataContainer().set(DataQuery.of("bar"), List.of(0, 2, 3)),
                        new DummyDataContainer().set(DataQuery.of("bar"), List.of(0, 1, 2))));
    }

    @Test
    void constructFromView() {
        final DataView expressionView = new DummyDataContainer();
        DataQuery query = DataQuery.of("object");
        expressionView.set(query, List.of(0, 3));
        assertTrue(new AllQueryExpression(expressionView, query)
                .query(DataQuery.of("foo", "0", "bar"), testContainer)
                .isPresent());
    }

    @Test
    void test() {
        assertFalse(new AllQueryExpression(Set.of(new ValueQueryExpression(0), new ValueQueryExpression(1)))
                .query(DataQuery.of("foo", "0", "bar"), testContainer)
                .isPresent());
        assertFalse(new AllQueryExpression(Set.of(
                        new ValueQueryExpression(0),
                        new ValueQueryExpression(1),
                        new ValueQueryExpression(2),
                        new ValueQueryExpression(3)))
                .query(DataQuery.of("foo", "0", "bar"), testContainer)
                .isPresent());
        var result = new AllQueryExpression(Set.of(new ValueQueryExpression(0), new ValueQueryExpression(2)))
                .query(DataQuery.of("foo", "1", "bar"), testContainer);
        assertTrue(result.isPresent());
        assertEquals(Type.ARRAY, result.get().type());
        assertEquals(2, result.get().size());
        assertEquals(Set.of("0", "2"), result.get().keySet());
    }
}
