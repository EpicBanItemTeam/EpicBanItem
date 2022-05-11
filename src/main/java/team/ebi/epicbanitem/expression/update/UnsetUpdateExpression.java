package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UnsetUpdateExpression implements UpdateExpression {

  private final DataQuery query;

  public UnsetUpdateExpression(DataQuery query) {
    this.query = DataQuery.of('.', query.last().toString());
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    ImmutableMap.Builder<DataQuery, UpdateOperation> builder = ImmutableMap.builder();
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      builder.put(query, UpdateOperation.remove(query));
    }
    return UpdateOperation.common(builder.build());
  }
}
