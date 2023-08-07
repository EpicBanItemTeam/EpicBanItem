/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExistsQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("string"), "bar");
        testContainer.set(DataQuery.of("number"), 123456.0);
        testContainer.createView(DataQuery.of("object")).set(DataQuery.of("foo"), "bar");
    }

    @Test
    void constructFromView() {
        DataQuery query = ExpressionQueries.EXISTS;
        for (ExistsQueryExpression expression : Sets.newHashSet(
                new ExistsQueryExpression(new DummyDataContainer().set(query, true), query),
                new ExistsQueryExpression(new DummyDataContainer().set(query, 1), query))) {
            assertTrue(expression
                    .query(DataQuery.of("object", "foo"), testContainer)
                    .isPresent());
        }
    }

    @Test
    void exist() {
        assertTrue(new ExistsQueryExpression(true)
                .query(DataQuery.of("object", "foo"), testContainer)
                .isPresent());
        assertFalse(new ExistsQueryExpression(false)
                .query(DataQuery.of("object", "foo"), testContainer)
                .isPresent());
    }
}
