package team.ebi.epicbanitem.expression.predicate;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonPredicateExpression;

public class NotPredicateExpression implements PredicateExpression {
  private final PredicateExpression expression;

  public NotPredicateExpression(DataView data) {
    this.expression = new CommonPredicateExpression(data);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(!expression.test(query, data).isPresent());
  }
}
