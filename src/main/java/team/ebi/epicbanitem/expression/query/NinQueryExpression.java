package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NinQueryExpression implements QueryExpression {
  private final QueryExpression expression;

  public NinQueryExpression(DataView data) {
    data.getViewList(DataQuery.of())
        .orElseThrow(() -> new InvalidDataException("$nin should be a array"));
    this.expression = new InQueryExpression(data);
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(!expression.query(query, data).isPresent());
  }
}
