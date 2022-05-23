/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.update;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetUpdateExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();
    private static DataQuery query;

    @BeforeAll
    static void beforeAll() {
        query = DataQuery.of("string");
        testContainer.set(query, "foo");
    }

    @Test
    void test() {
        UpdateOperation operation = new SetUpdateExpression(query, "bar").update(QueryResult.success(), testContainer);
        assertTrue(operation instanceof CommonUpdateOperation);
        UpdateOperation replace = operation.get(query);
        assertTrue(replace instanceof ReplaceUpdateOperation);
        assertEquals(new ReplaceUpdateOperation(query, "bar"), replace);
    }
}
