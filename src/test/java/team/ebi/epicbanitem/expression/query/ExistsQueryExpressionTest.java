package team.ebi.epicbanitem.expression.query;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;

class ExistsQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("string"), "bar");
    testContainer.set(DataQuery.of("number"), 123456.0);
    testContainer.createView(DataQuery.of("object")).set(DataQuery.of("foo"), "bar");
  }

  @Test
  void constructFromView() {
    DataQuery query = ExpressionQueries.EXISTS;
    for (ExistsQueryExpression expression :
        Set.of(
            new ExistsQueryExpression(new DummyDataContainer().set(query, true), query),
            new ExistsQueryExpression(new DummyDataContainer().set(query, 1), query))) {
      assertTrue(expression.query(DataQuery.of("object", "foo"), testContainer).isPresent());
    }
  }

  @Test
  void exist() {
    assertTrue(
        new ExistsQueryExpression(true)
            .query(DataQuery.of("object", "foo"), testContainer)
            .isPresent());
    assertTrue(
        new ExistsQueryExpression(false)
            .query(DataQuery.of("object", "foo"), testContainer)
            .isEmpty());
  }
}
