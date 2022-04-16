package team.ebi.epicbanitem.expression.query;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class ExtraQueryQueryExpression implements QueryExpression {

  private final QueryExpression expression;
  private final DataQuery query;

  public ExtraQueryQueryExpression(QueryExpression expression, DataQuery query) {
    this.query = query;
    this.expression = expression;
  }

  public ExtraQueryQueryExpression(DataView data, DataQuery query) {
    this(new CommonQueryExpression(data), query);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return expression
        .test(query.then(this.query), data)
        .map(it -> TestResult.success(ImmutableMap.of(this.query.toString(), it)));
  }
}
