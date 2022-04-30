package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NeQueryExpression implements QueryExpression {
  private final EqQueryExpression expression;

  public NeQueryExpression(Object data) {
    this.expression = new EqQueryExpression(data);
  }

  public NeQueryExpression(DataView data) {
    this.expression = new EqQueryExpression(data);
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(!expression.query(query, data).isPresent());
  }
}
