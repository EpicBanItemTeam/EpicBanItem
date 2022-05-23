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
import team.ebi.epicbanitem.api.expression.ExpressionQueries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("string"), "hello world");
    }

    @Test
    void constructFromView() {
        DataQuery query = ExpressionQueries.REGEX;
        assertTrue(new RegexQueryExpression(new DummyDataContainer().set(query, "/.*?world/"), query)
                .query(DataQuery.of("string"), testContainer)
                .isPresent());
        new RegexQueryExpression(new DummyDataContainer().set(query, "/.*?world/"), query);
    }

    @Test
    void regex() {
        assertTrue(new RegexQueryExpression("/.*?world/")
                .query(DataQuery.of("string"), testContainer)
                .isPresent());
        assertFalse(new RegexQueryExpression("/.*?halo/")
                .query(DataQuery.of("string"), testContainer)
                .isPresent());
    }
}
