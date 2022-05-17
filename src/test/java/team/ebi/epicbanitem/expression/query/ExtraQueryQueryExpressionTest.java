package team.ebi.epicbanitem.expression.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.expression.ExtraQueryQueryExpression;

class ExtraQueryQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("good", "foo"), 1);
    testContainer.set(DataQuery.of("bad", "foo"), 0);
  }

  @Test
  void eq() {
    ExtraQueryQueryExpression expression =
        new ExtraQueryQueryExpression(new EqQueryExpression(1), DataQuery.of("foo"));
    assertTrue(expression.query(DataQuery.of("good"), testContainer).isPresent());
    assertFalse(expression.query(DataQuery.of("bad"), testContainer).isPresent());
  }
}
