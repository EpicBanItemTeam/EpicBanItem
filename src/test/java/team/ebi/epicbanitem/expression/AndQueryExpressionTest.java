package team.ebi.epicbanitem.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.expression.query.AndQueryExpression;
import team.ebi.epicbanitem.expression.query.GtQueryExpression;
import team.ebi.epicbanitem.expression.query.LtQueryExpression;

class AndQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("number"), 5);
  }

  @Test
  void gtAndLt() {
    assertTrue(
        new AndQueryExpression(Set.of(new GtQueryExpression(4), new LtQueryExpression(6)))
            .query(DataQuery.of("number"), testContainer)
            .isPresent());

    assertFalse(
        new AndQueryExpression(Set.of(new GtQueryExpression(5), new LtQueryExpression(6)))
            .query(DataQuery.of("number"), testContainer)
            .isPresent());
  }
}
