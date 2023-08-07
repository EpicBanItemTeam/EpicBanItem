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
import team.ebi.epicbanitem.api.expression.ExpressionQueries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SizeQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("list"), Lists.newArrayList(1, 2, 3, 4));
    }

    // TODO DataView#getList can't get native type array
    @Test
    void constructFromView() {
        DataQuery query = ExpressionQueries.SIZE;
        assertTrue(new SizeQueryExpression(new DummyDataContainer().set(query, 4), query)
                .query(DataQuery.of("list"), testContainer)
                .isPresent());
        new RegexQueryExpression(new DummyDataContainer().set(query, "/.*?world/"), query);
    }

    @Test
    void size() {
        assertTrue(new SizeQueryExpression(4)
                .query(DataQuery.of("list"), testContainer)
                .isPresent());
        assertFalse(new SizeQueryExpression(5)
                .query(DataQuery.of("list"), testContainer)
                .isPresent());
    }
}
