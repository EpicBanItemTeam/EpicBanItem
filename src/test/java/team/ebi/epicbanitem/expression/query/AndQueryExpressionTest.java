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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("number"), 5);
    }

    @Test
    void gtAndLt() {
        assertTrue(new AndQueryExpression(Sets.newHashSet(new GtQueryExpression(4), new LtQueryExpression(6)))
                .query(DataQuery.of("number"), testContainer)
                .isPresent());

        assertFalse(new AndQueryExpression(Sets.newHashSet(new GtQueryExpression(5), new LtQueryExpression(6)))
                .query(DataQuery.of("number"), testContainer)
                .isPresent());
    }
}
