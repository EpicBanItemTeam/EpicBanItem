package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class ExtraQueryQueryExpression implements QueryExpression {

  private final QueryExpression expression;
  private final DataQuery query;

  public ExtraQueryQueryExpression(QueryExpression expression, DataQuery query) {
    List<DataQuery> queries = query.queryParts();
    if (queries.size() > 1) {
      this.query = queries.get(0);
      this.expression =
          new ArrayableQueryExpression(new ExtraQueryQueryExpression(expression, query.popFirst()));
    } else {
      this.expression = expression;
      this.query = query;
    }
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return expression
        .query(query.then(this.query), data)
        .map(it -> QueryResult.success(ImmutableMap.of(this.query.toString(), it)));
  }
}
