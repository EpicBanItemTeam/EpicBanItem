package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class SizeQueryExpression implements QueryExpression {
  private final int size;

  public SizeQueryExpression(DataView data, DataQuery query) {
    this.size =
        data.getInt(query)
            .orElseThrow(() -> new InvalidDataException("$size should be int. Current: null"));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    int size = data.getList(query).map(List::size).orElse(-1);
    return QueryResult.from(size >= 0 && size == this.size);
  }
}
