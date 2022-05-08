package team.ebi.epicbanitem.expression;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.BinaryOperator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class MathUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final DataQuery first;
  private final Number argNumber;
  private final BinaryOperator<Number> operator;

  public MathUpdateExpression(DataView data, BinaryOperator<Number> operator) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    this.first = query.queryParts().get(0);
    this.argNumber =
        data.get(DataQuery.of())
            .filter(it -> it instanceof Number)
            .map(it -> (Number) it)
            .orElseThrow(() -> new InvalidDataException("Input not a valid number"));
    this.operator = operator;
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    DataContainer container = DataContainer.createNew();
    data.getView(first).ifPresent(it -> container.set(first, it));
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      DataView view =
          container
              .getView(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("Set {} to container failed", query)));
      Optional<Object> sourceOptional = view.get(DataQuery.of());
      Number sourceNumber = (Number) sourceOptional.orElse(0);
      container.set(query, this.operator.apply(argNumber, sourceNumber));
      updateOperation = updateOperation.merge(UpdateOperation.replace(view));
    }

    return updateOperation;
  }
}
