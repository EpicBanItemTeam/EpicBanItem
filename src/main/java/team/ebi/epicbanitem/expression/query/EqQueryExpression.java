package team.ebi.epicbanitem.expression.query;

import java.util.Objects;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataViewUtils;

public class EqQueryExpression implements QueryExpression {
  private final Object value;

  public EqQueryExpression(Object data) {
    if (data instanceof Number) data = ((Number) data).doubleValue();
    this.value = data;
  }

  public EqQueryExpression(DataView data, DataQuery query) {
    this.value = data.get(query).orElseThrow(() -> new InvalidDataException("$eq need a value"));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    Object value = DataViewUtils.get(data, query).orElse(null);
    if (value instanceof Number) value = ((Number) value).doubleValue();
    return QueryResult.from(Objects.equals(this.value, value));
  }
}
