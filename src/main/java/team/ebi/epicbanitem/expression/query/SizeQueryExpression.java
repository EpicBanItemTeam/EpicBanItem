package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataPreconditions;

public class SizeQueryExpression implements QueryExpression {
  private final int size;

  public SizeQueryExpression(DataView data) {
    Optional<Integer> optional = data.getInt(DataQuery.of());
    DataPreconditions.checkData(optional.isPresent(), "$size should be int. Current: null");
    this.size = optional.get();
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    int size;
    if (data instanceof DataView) {
      size = ((DataView) data).getList(query).map(List::size).orElse(-1);
    } else {
      size = ((List<?>) data).size();
    }
    return QueryResult.from(size >= 0 && size == this.size);
  }
}
