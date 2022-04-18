package team.ebi.epicbanitem.expression.update;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class SetUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final DataQuery first;
  private final DataView value;

  public SetUpdateExpression(DataView data) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    this.first = query.queryParts().get(0);
    this.value = data;
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    DataContainer container = DataContainer.createNew();
    data.getView(first).ifPresent(it -> container.set(first, it));
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      container.set(query, value);
      DataView view =
          container
              .getView(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          String.format("Set %s to container failed", query)));
      updateOperation = updateOperation.merge(UpdateOperation.replace(view));
    }

    return updateOperation;
  }
}
