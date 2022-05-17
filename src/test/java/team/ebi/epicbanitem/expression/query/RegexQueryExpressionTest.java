package team.ebi.epicbanitem.expression.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;

class RegexQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(DataQuery.of("string"), "hello world");
  }

  @Test
  void constructFromView() {
    DataQuery query = ExpressionQueries.REGEX;
    assertTrue(
        new RegexQueryExpression(new DummyDataContainer().set(query, "/.*?world/"), query)
            .query(DataQuery.of("string"), testContainer)
            .isPresent());
    new RegexQueryExpression(new DummyDataContainer().set(query, "/.*?world/"), query);
  }

  @Test
  void regex() {
    assertTrue(
        new RegexQueryExpression("/.*?world/")
            .query(DataQuery.of("string"), testContainer)
            .isPresent());
    assertFalse(
        new RegexQueryExpression("/.*?halo/")
            .query(DataQuery.of("string"), testContainer)
            .isPresent());
  }
}
