package team.ebi.epicbanitem.expression.predicate;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;

public class NinPredicateExpression implements PredicateExpression {
  private final PredicateExpression expression;

  public NinPredicateExpression(DataView data) {
    data.getViewList(DataQuery.of())
        .orElseThrow(() -> new InvalidDataException("$nin should be a array"));
    this.expression = new InPredicateExpression(data);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(!expression.test(query, data).isPresent());
  }
}
