/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression.update;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;
import team.ebi.epicbanitem.expression.update.PopUpdateExpression.Position;

import static org.junit.jupiter.api.Assertions.*;

class PopUpdateExpressionTest {

    private static final DataView testContainer = new DummyDataContainer();
    private static DataQuery query;

    @BeforeAll
    static void beforeAll() {
        query = DataQuery.of("array");
        testContainer.set(query, new int[] {0, 1, 2});
    }

    @Test
    void popFirst() {
        UpdateOperation operation =
                new PopUpdateExpression(query, Position.FIRST).update(QueryResult.success(), testContainer);
        assertTrue(operation instanceof CommonUpdateOperation);
        UpdateOperation replace = operation.get(query);
        assertTrue(replace instanceof ReplaceUpdateOperation);
        assertNotEquals(new ReplaceUpdateOperation(query, Lists.newArrayList(1, 2)), replace);
        assertArrayEquals(new int[] {1, 2}, (int[]) ((ReplaceUpdateOperation) replace).value());
    }
}
