/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.query;

import java.util.Optional;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ElemExistsQueryExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();

    @BeforeAll
    static void beforeAll() {
        testContainer.set(DataQuery.of("foo"), Lists.newArrayList(1, 2, 3));
    }

    @Test
    void test() {
        ElemMatchQueryExpression expression = new ElemMatchQueryExpression(new GtQueryExpression(2));
        Optional<QueryResult> result = expression.query(DataQuery.of("foo"), testContainer);
        assertTrue(result.isPresent());
        assertTrue(result.get().containsKey("2"));
    }
}
