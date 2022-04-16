package team.ebi.epicbanitem.expression.predicate;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonPredicateExpression;

public class ExtraQueryPredicateExpression implements PredicateExpression {

  private final PredicateExpression expression;
  private final DataQuery query;

  public ExtraQueryPredicateExpression(PredicateExpression expression, DataQuery query) {
    this.query = query;
    this.expression = expression;
  }

  public ExtraQueryPredicateExpression(DataView data, DataQuery query) {
    this(new CommonPredicateExpression(data), query);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return expression
        .test(query.then(this.query), data)
        .map(it -> TestResult.success(ImmutableMap.of(this.query.toString(), it)));
  }
}
