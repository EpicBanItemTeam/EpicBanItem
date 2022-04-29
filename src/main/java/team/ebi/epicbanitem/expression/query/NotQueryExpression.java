package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class NotQueryExpression implements QueryExpression {
  private final QueryExpression expression;

  public NotQueryExpression(DataView data) {
    this.expression = new CommonQueryExpression(data);
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    return QueryResult.from(!expression.query(query, data).isPresent());
  }
}
