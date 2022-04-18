package team.ebi.epicbanitem.expression;

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
import team.ebi.epicbanitem.util.DataPreconditions;

public class MathUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final DataQuery first;
  private final Number argNumber;
  private final BinaryOperator<Number> operator;

  public MathUpdateExpression(DataView data, BinaryOperator<Number> operator) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    this.first = query.queryParts().get(0);
    Object input =
        data.get(DataQuery.of()).orElseThrow(() -> new InvalidDataException("Input can't get"));
    DataPreconditions.checkData(input instanceof Number, "Input should be a number");
    this.argNumber = (Number) input;
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
                          String.format("Set %s to container failed", query)));
      Optional<Object> sourceOptional = view.get(DataQuery.of());
      Number sourceNumber = (Number) sourceOptional.orElse(0);
      container.set(query, this.operator.apply(argNumber, sourceNumber));
      updateOperation = updateOperation.merge(UpdateOperation.replace(view));
    }

    return updateOperation;
  }
}
