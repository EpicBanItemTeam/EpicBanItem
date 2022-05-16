package team.ebi.epicbanitem.expression.query;

import java.util.Objects;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.data.DataUtils;

public class EqQueryExpression implements QueryExpression {

  private final Object value;

  public EqQueryExpression(Object data) {
    if (data instanceof Number n) {
      data = n.doubleValue();
    } else if (data instanceof DataView view) {
      data = view.copy();
    }
    this.value = data;
  }

  public EqQueryExpression(DataView data, DataQuery query) {
    this(data.get(query).orElseThrow(() -> new InvalidDataException("$eq can't find valid value")));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    Object current = DataUtils.get(data, query).orElse(null);
    if (current instanceof Number n) {
      current = n.doubleValue();
    } else if (current instanceof DataView view) {
      current = view.copy();
    }
    return QueryResult.from(Objects.equals(value, current));
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, value);
  }
}
