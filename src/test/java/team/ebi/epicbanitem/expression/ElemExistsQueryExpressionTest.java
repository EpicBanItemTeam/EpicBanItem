package team.ebi.epicbanitem.expression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.DummyDataContainer;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.query.ElemMatchQueryExpression;
import team.ebi.epicbanitem.expression.query.GtQueryExpression;

class ElemExistsQueryExpressionTest {

  private static final DataView testContainer = new DummyDataContainer();

  @BeforeAll
  static void beforeAll() {
    testContainer.set(
        DataQuery.of("foo"),
        List.of(1, 2, 3));
  }

  @Test
  void test() {
    ElemMatchQueryExpression expression = new ElemMatchQueryExpression(new GtQueryExpression(2));
    Optional<QueryResult> result = expression.query(DataQuery.of("foo"), testContainer);
    assertTrue(result.isPresent());
    assertTrue(result.get().containsKey("2"));
  }
}
