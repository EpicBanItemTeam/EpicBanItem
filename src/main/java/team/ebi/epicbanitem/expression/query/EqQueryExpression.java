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
    this.value = data;
  }

  public EqQueryExpression(DataView data) {
    this.value =
        data.get(DataQuery.of()).orElseThrow(() -> new InvalidDataException("$eq need a value"));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    Object value = DataViewUtils.get(data, query).orElse(null);
    return QueryResult.from(Objects.equals(this.value, value));
  }
}
