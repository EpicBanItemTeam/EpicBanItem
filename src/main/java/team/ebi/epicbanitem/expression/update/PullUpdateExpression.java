package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.text.MessageFormat;
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
  private final QueryExpression expression;

  public PullUpdateExpression(DataView view, DataQuery query) {
    this.query = DataQuery.of('.', query.last().toString());
    this.expression = new CommonQueryExpression(view, query);
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    var builder = ImmutableMap.<DataQuery, UpdateOperation>builder();
    for (DataQuery currentQuery : UpdateExpression.parseQuery(query, result)) {
      var list =
          data.getList(currentQuery)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("$pull failed, {0} is invalid list", currentQuery)));
      var finalList = ImmutableList.builder();
      for (int i = 0; i < list.size(); i++) {
        var subResult = expression.query(currentQuery.then(String.valueOf(i)), data);
        if (subResult.isPresent()) {
          finalList.add(list.get(i));
        }
      }
      builder.put(currentQuery, UpdateOperation.replace(currentQuery, list));
    }
    return UpdateOperation.common(builder.build());
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, expression);
  }
}
