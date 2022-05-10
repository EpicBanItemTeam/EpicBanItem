package team.ebi.epicbanitem.expression.update;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UnsetUpdateExpression implements UpdateExpression {

  private final DataQuery query;

  public UnsetUpdateExpression(DataView data) {
    this.query = DataQuery.of('.', data.currentPath().toString());
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      updateOperation = updateOperation.merge(UpdateOperation.remove(query));
    }
    return updateOperation;
  }
}
