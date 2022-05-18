package team.ebi.epicbanitem.expression.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonUpdateOperation;
import team.ebi.epicbanitem.expression.RemoveUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;

class RenameUpdateExpressionTest {
  private static final DataView testContainer = new DummyDataContainer();
  private static DataQuery query;

  @BeforeAll
  static void beforeAll() {
    query = DataQuery.of("string");
    testContainer.set(query, "foo");
  }

  @Test
  void test() {
    DataQuery target = DataQuery.of("bar");
    UpdateOperation operation =
        new RenameUpdateExpression(query, target).update(QueryResult.success(), testContainer);
    assertTrue(operation instanceof CommonUpdateOperation);
    UpdateOperation remove = operation.get(query);
    assertTrue(remove instanceof RemoveUpdateOperation);
    assertEquals(new RemoveUpdateOperation(query), remove);

    UpdateOperation replace = operation.get(target);
    assertTrue(replace instanceof ReplaceUpdateOperation);
    assertEquals(new ReplaceUpdateOperation(target, testContainer.get(query).get()), replace);
  }
}
