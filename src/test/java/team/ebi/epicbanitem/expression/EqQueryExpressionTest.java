package team.ebi.epicbanitem.expression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.expression.query.EqQueryExpression;

class EqQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("string"), "bar");
    testContainer.set(DataQuery.of("number"), 123456.0);
    testContainer.createView(DataQuery.of("object")).set(DataQuery.of("foo"), "bar");
  }

  @Test
  void constructFromView() {
    final DataView expressionView = new DummyDataContainer();
    DataQuery query = DataQuery.of("string");
    expressionView.set(query, "bar");
    final EqQueryExpression expression = new EqQueryExpression(expressionView, query);
    assertTrue(expression.query(DataQuery.of("string"), testContainer).isPresent());
    assertTrue(expression.query(DataQuery.of("number"), testContainer).isEmpty());
  }

  @Test
  void string() {
    EqQueryExpression expression = new EqQueryExpression("bar");
    assertTrue(expression.query(DataQuery.of("string"), testContainer).isPresent());
    assertTrue(expression.query(DataQuery.of("number"), testContainer).isEmpty());
  }

  @Test
  void number() {
    EqQueryExpression expression = new EqQueryExpression(123456);
    assertTrue(expression.query(DataQuery.of("number"), testContainer).isPresent());
    assertTrue(expression.query(DataQuery.of("string"), testContainer).isEmpty());
  }

  @Test
  void object() {
    final DataView object = new DummyDataContainer();
    object.set(DataQuery.of("foo"), "bar");

    EqQueryExpression expression = new EqQueryExpression(object);
    assertTrue(expression.query(DataQuery.of("object"), testContainer).isPresent());
    assertTrue(expression.query(DataQuery.of("number"), testContainer).isEmpty());
  }
}
