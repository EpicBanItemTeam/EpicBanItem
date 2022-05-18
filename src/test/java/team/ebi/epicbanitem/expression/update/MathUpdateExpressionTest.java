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
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;

class MathUpdateExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();
  private static DataQuery query;

  @BeforeAll
  static void beforeAll() {
    query = DataQuery.of("number");
    testContainer.set(query, (short) 5);
  }

  @Test
  void inc() {
    IncUpdateExpression expression = new IncUpdateExpression(query, -1);
    UpdateOperation operation = expression.update(QueryResult.success(), testContainer);
    assertTrue(operation instanceof CommonUpdateOperation);
    UpdateOperation replace = operation.get(query);
    assertTrue(replace instanceof ReplaceUpdateOperation);
    assertTrue(((ReplaceUpdateOperation) replace).value() instanceof Short);
    assertEquals((short) 4, ((ReplaceUpdateOperation) replace).value());
  }

  @Test
  void mul() {
    MulUpdateExpression expression = new MulUpdateExpression(query, -2);
    UpdateOperation operation = expression.update(QueryResult.success(), testContainer);
    assertTrue(operation instanceof CommonUpdateOperation);
    UpdateOperation replace = operation.get(query);
    assertTrue(replace instanceof ReplaceUpdateOperation);
    // Keep the type
    assertTrue(((ReplaceUpdateOperation) replace).value() instanceof Short);
    assertEquals((short) -10, ((ReplaceUpdateOperation) replace).value());
  }
}
