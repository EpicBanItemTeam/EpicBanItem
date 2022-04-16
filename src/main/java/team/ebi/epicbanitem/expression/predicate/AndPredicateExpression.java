package team.ebi.epicbanitem.expression.predicate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonPredicateExpression;

public class AndPredicateExpression implements PredicateExpression {

  private final Set<PredicateExpression> expressions;

  public AndPredicateExpression(DataView data) {
    List<DataView> views =
        data.getViewList(DataQuery.of())
            .orElseThrow(() -> new InvalidDataException("$and should be a array"));
    this.expressions =
        views.stream().map(CommonPredicateExpression::new).collect(Collectors.toSet());
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    TestResult result = TestResult.success();
    for (PredicateExpression expression : this.expressions) {
      Optional<TestResult> currentResult = expression.test(query, data);
      if (currentResult.isPresent()) result = result.merge(currentResult.get());
      else return TestResult.failed();
    }
    return Optional.of(result);
  }
}
