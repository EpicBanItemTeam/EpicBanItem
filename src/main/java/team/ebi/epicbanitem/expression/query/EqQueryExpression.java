package team.ebi.epicbanitem.expression.query;

import java.util.Objects;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class EqQueryExpression implements QueryExpression {
  private final Object value;

  public EqQueryExpression(Object data) {
    this.value = data;
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    Object value = data instanceof DataView ? ((DataView) data).get(query).orElse(null) : data;
    return QueryResult.from(Objects.equals(this.value, value));
  }
}
