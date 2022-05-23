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
import team.ebi.epicbanitem.expression.ExtraQueryQueryExpression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraQueryQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("good", "foo"), 1);
        testContainer.set(DataQuery.of("bad", "foo"), 0);
    }

    @Test
    void eq() {
        ExtraQueryQueryExpression expression =
                new ExtraQueryQueryExpression(new EqQueryExpression(1), DataQuery.of("foo"));
        assertTrue(expression.query(DataQuery.of("good"), testContainer).isPresent());
        assertFalse(expression.query(DataQuery.of("bad"), testContainer).isPresent());
    }
}
