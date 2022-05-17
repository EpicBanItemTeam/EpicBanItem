package team.ebi.epicbanitem.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.expression.query.InQueryExpression;

class InQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("foo"), 1);
    testContainer.set(DataQuery.of("bar"), "1");
  }

  @Test
  void constructFromView() {
    final DataView expressionView = new DummyDataContainer();
    DataQuery query = DataQuery.of("array");
    expressionView.set(query, List.of(0, "1"));
    assertFalse(
        new InQueryExpression(expressionView, query)
            .query(DataQuery.of("foo"), testContainer)
            .isPresent());
  }

  @Test
  void test() {
    InQueryExpression expression = new InQueryExpression(
        List.of(new ValueQueryExpression("1"), new ValueQueryExpression(0)));
    assertTrue(expression.query(DataQuery.of("bar"), testContainer).isPresent());
    assertFalse(expression.query(DataQuery.of("foo"), testContainer).isPresent());
  }
}
