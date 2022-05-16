package team.ebi.epicbanitem.expression;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;
import team.ebi.epicbanitem.expression.query.RegexQueryExpression;
import team.ebi.epicbanitem.expression.query.SizeQueryExpression;

class SizeQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("list"), new int[]{0, 0, 0, 0});
  }

  // TODO DataView#getList can't get native type array
  @Test
  void constructFromView() {
    DataQuery query = ExpressionQueries.SIZE;
    assertTrue(new SizeQueryExpression(new DummyDataContainer().set(query, 4), query).query(
        DataQuery.of("list"), testContainer).isPresent());
    new RegexQueryExpression(new DummyDataContainer().set(query, "/.*?world/"), query);
  }

  @Test
  void size() {
    assertTrue(new SizeQueryExpression(4).query(DataQuery.of("list"), testContainer).isPresent());
    assertFalse(new SizeQueryExpression(5).query(DataQuery.of("list"), testContainer).isPresent());
  }
}
