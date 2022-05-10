package team.ebi.epicbanitem.expression.update;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class SetUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final DataView value;

  public SetUpdateExpression(DataView data) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    this.value = data;
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    for (DataQuery query : UpdateExpression.parseQuery(query, result))
      updateOperation = updateOperation.merge(UpdateOperation.replace(query, value));
    return updateOperation;
  }
}
