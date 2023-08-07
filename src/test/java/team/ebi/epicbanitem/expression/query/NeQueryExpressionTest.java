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

class NeQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("string"), "bar");
        testContainer.set(DataQuery.of("number"), 123456.0);
        testContainer.createView(DataQuery.of("object")).set(DataQuery.of("foo"), "bar");
    }

    @Test
    void constructFromView() {
        final DataView expressionView = new DummyDataContainer();
        DataQuery query = DataQuery.of("string");
        expressionView.set(query, "bar");
        final NeQueryExpression expression = new NeQueryExpression(expressionView, query);
        assertFalse(expression.query(DataQuery.of("string"), testContainer).isPresent());
        assertTrue(expression.query(DataQuery.of("number"), testContainer).isPresent());
    }

    @Test
    void string() {
        NeQueryExpression stringEq = new NeQueryExpression("bar");
        assertFalse(stringEq.query(DataQuery.of("string"), testContainer).isPresent());
        assertTrue(stringEq.query(DataQuery.of("number"), testContainer).isPresent());
    }

    @Test
    void number() {
        NeQueryExpression numberEq = new NeQueryExpression(123456);
        assertFalse(numberEq.query(DataQuery.of("number"), testContainer).isPresent());
        assertTrue(numberEq.query(DataQuery.of("string"), testContainer).isPresent());
    }

    @Test
    void object() {
        final DataView object = new DummyDataContainer();
        object.set(DataQuery.of("foo"), "bar");

        NeQueryExpression objectEq = new NeQueryExpression(object);
        assertFalse(objectEq.query(DataQuery.of("object"), testContainer).isPresent());
        assertTrue(objectEq.query(DataQuery.of("number"), testContainer).isPresent());
    }
}
