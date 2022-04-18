package team.ebi.epicbanitem.expression.update;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UnsetUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final DataQuery first;

  public UnsetUpdateExpression(DataView data) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    this.first = query.queryParts().get(0);
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    DataContainer container = DataContainer.createNew();
    data.getView(first).ifPresent(it -> container.set(first, it));
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      container.remove(query);
      if (container.contains(query))
        throw new UnsupportedOperationException(
            String.format("Remove %s from container failed", query));
      updateOperation = updateOperation.merge(UpdateOperation.remove(query));
    }
    return updateOperation;
  }
}
