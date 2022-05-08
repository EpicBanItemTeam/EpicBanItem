package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.RemoveUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;

public class RenameUpdateExpression implements UpdateExpression {

  private final DataQuery source;
  private final DataQuery target;

  public RenameUpdateExpression(DataView data) {
    this.source = DataQuery.of('.', data.currentPath().toString());
    this.target =
        DataQuery.of(
            '.',
            data.getString(DataQuery.of())
                .orElseThrow(() -> new InvalidDataException("$rename need value be string")));
    if (source.equals(target))
      throw new InvalidDataException(
          MessageFormat.format("$rename with the same source and target query: %s", target));
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    List<DataQuery> targetQueries = UpdateExpression.parseQuery(target, result);
    if (targetQueries.size() > 1 || !targetQueries.get(0).equals(target))
      throw new InvalidDataException("$rename target is dynamic array query");

    List<DataQuery> sourceQueries = UpdateExpression.parseQuery(source, result);
    if (sourceQueries.size() > 1 || !sourceQueries.get(0).equals(source))
      throw new InvalidDataException("$rename source is dynamic array query");

    Optional<DataView> view = data.getView(source);
    DataContainer container = DataContainer.createNew();
    UpdateOperation operation = UpdateOperation.common();
    if (view.isPresent()) {
      container.set(target, view.get());
      //noinspection OptionalGetWithoutIsPresent
      operation =
          UpdateOperation.common(
              ImmutableMap.of(
                  source, new RemoveUpdateOperation(source),
                  target, new ReplaceUpdateOperation(container.getView(target).get())));
    }
    return operation;
  }
}
