package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class NinQueryExpression implements QueryExpression {
  private final QueryExpression expression;

  public NinQueryExpression(List<QueryExpression> expressions) {
    this.expression = new InQueryExpression(expressions);
  }

  public NinQueryExpression(DataView data, DataQuery query) {
    if (data.getList(query).isEmpty()) {
      throw new InvalidDataException("$nin should be an array");
    }
    this.expression = new InQueryExpression(data, query);
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(expression.query(query, data).isEmpty());
  }

  @Override
  public DataContainer toContainer() {
    return expression.toContainer();
  }
}
