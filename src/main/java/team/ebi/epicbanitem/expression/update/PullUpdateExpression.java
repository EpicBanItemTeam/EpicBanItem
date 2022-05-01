package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class PullUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final DataQuery first;
  private final QueryExpression expression;

  public PullUpdateExpression(DataQuery query, QueryExpression expression) {
    this.query = query;
    this.first = query.queryParts().get(0);
    this.expression = expression;
  }

  public PullUpdateExpression(DataView data) {
    this(DataQuery.of('.', data.currentPath().toString()), new CommonQueryExpression(data));
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    DataContainer container = DataContainer.createNew();
    data.getView(first).ifPresent(it -> container.set(first, it));
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      DataView view =
          container
              .getView(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("$pull failed, {} is invalid", query)));

      List<?> values =
          view.getList(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("$pull failed, {} isn't an array", query)));
      ImmutableList.Builder<Object> newValues = ImmutableList.builder();
      for (int i = 0; i < values.size(); i++) {
        Optional<QueryResult> subResult = expression.query(query.then(String.valueOf(i)), data);
        if (subResult.isPresent()) newValues.add(values.get(i));
      }
      view.set(query, newValues.build());
      operation = operation.merge(UpdateOperation.replace(view));
    }

    return operation;
  }
}
