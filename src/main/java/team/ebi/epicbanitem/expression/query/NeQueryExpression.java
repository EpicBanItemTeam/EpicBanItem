package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NeQueryExpression implements QueryExpression {
  private final DataView value;

  public NeQueryExpression(DataView data) {
    this.value = data;
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(!value.equals(data.getView(query).orElse(null)));
  }
}
