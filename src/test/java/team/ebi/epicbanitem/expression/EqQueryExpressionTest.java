package team.ebi.epicbanitem.expression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.query.EqQueryExpression;

class EqQueryExpressionTest {

  private static
  final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("string"), "bar");
    testContainer.set(DataQuery.of("number"), 123456.0);
    testContainer.createView(DataQuery.of("object")).set(DataQuery.of("foo"), "bar");
  }

  @Test
  void string() {
    EqQueryExpression stringEq = new EqQueryExpression("bar");
    assertTrue(stringEq.query(DataQuery.of("string"), testContainer).isPresent());
  }

  @Test
  void number() {
    EqQueryExpression numberEq = new EqQueryExpression(123456);
    assertTrue(numberEq.query(DataQuery.of("number"), testContainer).isPresent());
  }

  @Test
  void object() {
    final DataView object = new DummyDataContainer();
    object.set(DataQuery.of("foo"), "bar");

    EqQueryExpression objectEq = new EqQueryExpression(object);
    Optional<QueryResult> result = objectEq.query(DataQuery.of("object"), testContainer);
    assertTrue(result.isPresent());
  }
}
