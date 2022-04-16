package team.ebi.epicbanitem.expression.predicate;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;

public class NorPredicateExpression implements PredicateExpression {
  private final PredicateExpression expression;

  public NorPredicateExpression(DataView data) {
    data.getViewList(DataQuery.of())
        .orElseThrow(() -> new InvalidDataException("$nor should be a array"));
    this.expression = new OrPredicateExpression(data);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(!expression.test(query, data).isPresent());
  }
}
