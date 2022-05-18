package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class SetUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final Object value;

  public SetUpdateExpression(DataQuery query, Object value) {
    this.query = query;
    this.value = value;
  }

  public SetUpdateExpression(DataView view, DataQuery query) throws InvalidDataException {
    this.query = DataQuery.of('.', query.last().toString());
    this.value =
        view.get(query).orElseThrow(() -> new InvalidDataException(query + "need a value"));
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    ImmutableMap.Builder<DataQuery, UpdateOperation> builder = ImmutableMap.builder();
    for (DataQuery currentQuery : UpdateExpression.parseQuery(query, result)) {
      builder.put(currentQuery, UpdateOperation.replace(currentQuery, value));
    }
    return UpdateOperation.common(builder.build());
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, value);
  }
}
