package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NinQueryExpression implements QueryExpression {
  private final QueryExpression expression;

  public NinQueryExpression(DataView data, DataQuery query) {
    data.getViewList(query)
        .orElseThrow(() -> new InvalidDataException("$nin should be a objects array"));
    this.expression = new InQueryExpression(data, query);
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(!expression.query(query, data).isPresent());
  }
}
