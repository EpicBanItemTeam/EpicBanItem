package team.ebi.epicbanitem.expression.update;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;
import team.ebi.epicbanitem.expression.query.GteQueryExpression;

class PullUpdateExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();
  private static DataQuery query;

  @BeforeAll
  static void beforeAll() {
    query = DataQuery.of("array");
    testContainer.set(query, new int[] {0, 1, 2});
  }

  @Test
  void pull() {
    UpdateOperation operation =
        new PullUpdateExpression(query, new GteQueryExpression(1))
            .update(QueryResult.success(), testContainer);
    assertTrue(operation instanceof CommonUpdateOperation);
    UpdateOperation replace = operation.get(query);
    assertTrue(replace instanceof ReplaceUpdateOperation);
    assertNotEquals(new ReplaceUpdateOperation(query, List.of(1, 2)), replace);
    assertArrayEquals(new int[] {1, 2}, (int[]) ((ReplaceUpdateOperation) replace).value());
  }
}
