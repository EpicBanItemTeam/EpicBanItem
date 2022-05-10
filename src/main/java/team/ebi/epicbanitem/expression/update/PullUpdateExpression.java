package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class PullUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final QueryExpression expression;

  public PullUpdateExpression(DataQuery query, QueryExpression expression) {
    this.query = query;
    this.expression = expression;
  }

  public PullUpdateExpression(DataView data) {
    this(
        DataQuery.of('.', data.currentPath().toString()),
        new CommonQueryExpression(data, DataQuery.of()));
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      List<?> list =
          data.getList(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("$pull failed, {0} is invalid", query)));
      ImmutableList.Builder<Object> finalList = ImmutableList.builder();
      for (int i = 0; i < list.size(); i++) {
        Optional<QueryResult> subResult = expression.query(query.then(String.valueOf(i)), data);
        if (subResult.isPresent()) finalList.add(list.get(i));
      }
      operation = operation.merge(UpdateOperation.replace(query, finalList));
    }
    return operation;
  }
}
