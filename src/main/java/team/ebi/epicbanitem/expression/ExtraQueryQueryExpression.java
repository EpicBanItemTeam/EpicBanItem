package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

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
  public Optional<QueryResult> query(DataQuery query, Object data) {
    return expression
        .query(query.then(this.query), data)
        .map(it -> QueryResult.success(ImmutableMap.of(this.query.toString(), it)));
  }
}
