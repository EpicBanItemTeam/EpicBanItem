/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("number"), 15.0);
    }

    @Test
    void constructFromView() {
        final DataView expressionView = new DummyDataContainer();
        DataQuery query = DataQuery.of("number");
        expressionView.set(query, 14);
        assertTrue(new GtQueryExpression(expressionView, query)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
    }

    @Test
    void gt() {
        assertFalse(new GtQueryExpression(15)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
        assertTrue(new GtQueryExpression(14)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
    }

    @Test
    void gte() {
        assertFalse(new GteQueryExpression(16)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
        assertTrue(new GteQueryExpression(15)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
        assertTrue(new GteQueryExpression(14)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
    }

    @Test
    void lt() {
        assertTrue(new LtQueryExpression(16)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
        assertFalse(new LtQueryExpression(15)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
    }

    @Test
    void lte() {
        assertTrue(new LteQueryExpression(16)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
        assertTrue(new LteQueryExpression(15)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
        assertFalse(new LteQueryExpression(14)
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
    }
}
