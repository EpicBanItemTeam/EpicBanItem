package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class AndQueryExpression implements QueryExpression {

  private final Set<QueryExpression> expressions;

  public AndQueryExpression(DataView data) {
    List<DataView> views =
        data.getViewList(DataQuery.of())
            .orElseThrow(() -> new InvalidDataException("$and should be a array"));
    this.expressions =
        views.stream().map(CommonQueryExpression::new).collect(Collectors.toSet());
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    TestResult result = TestResult.success();
    for (QueryExpression expression : this.expressions) {
      Optional<TestResult> currentResult = expression.test(query, data);
      if (currentResult.isPresent()) result = result.merge(currentResult.get());
      else return TestResult.failed();
    }
    return Optional.of(result);
  }
}
