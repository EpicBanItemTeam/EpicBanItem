package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NorQueryExpression implements QueryExpression {
  private final QueryExpression expression;

  public NorQueryExpression(DataView data) {
    data.getViewList(DataQuery.of())
        .orElseThrow(() -> new InvalidDataException("$nor should be a array"));
    this.expression = new OrQueryExpression(data);
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    return QueryResult.from(!expression.query(query, data).isPresent());
  }
}
