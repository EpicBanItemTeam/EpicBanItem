/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.expression.ValueQueryExpression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("foo"), 1);
        testContainer.set(DataQuery.of("bar"), "1");
    }

    @Test
    void constructFromView() {
        final DataView expressionView = new DummyDataContainer();
        DataQuery query = DataQuery.of("array");
        expressionView.set(query, Lists.newArrayList(0, "1"));
        assertFalse(new InQueryExpression(expressionView, query)
                .query(DataQuery.of("foo"), testContainer)
                .isPresent());
    }

    @Test
    void test() {
        InQueryExpression expression =
                new InQueryExpression(Lists.newArrayList(new ValueQueryExpression("1"), new ValueQueryExpression(0)));
        assertTrue(expression.query(DataQuery.of("bar"), testContainer).isPresent());
        assertFalse(expression.query(DataQuery.of("foo"), testContainer).isPresent());
    }
}
