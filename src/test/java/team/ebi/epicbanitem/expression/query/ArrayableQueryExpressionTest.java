package team.ebi.epicbanitem.expression.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.ArrayableQueryExpression;

class ArrayableQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("bad"), List.of(0, 2));
    testContainer.set(DataQuery.of("array"), List.of(0, 1, 2));
    testContainer.set(DataQuery.of("number"), 1);
  }

  @Test
  void eq() {
    ArrayableQueryExpression expression = new ArrayableQueryExpression(new EqQueryExpression(1));
    Optional<QueryResult> arrayResult = expression.query(DataQuery.of("array"), testContainer);
    assertTrue(arrayResult.isPresent());
    assertTrue(arrayResult.get().containsKey("1"));
    assertFalse(expression.query(DataQuery.of("bad"), testContainer).isPresent());
    assertTrue(expression.query(DataQuery.of("number"), testContainer).isPresent());
  }
}
