package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.BinaryOperator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Coerce;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class MathUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final Number argNumber;
  private final BinaryOperator<Number> operator;

  public MathUpdateExpression(DataView view, DataQuery query, BinaryOperator<Number> operator) {
    this.query = DataQuery.of('.', query.last().toString());
    this.argNumber =
        view.get(query)
            .filter(Number.class::isInstance)
            .map(Number.class::cast)
            .orElseThrow(() -> new InvalidDataException(query + "need a number input"));
    this.operator = operator;
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    ImmutableMap.Builder<DataQuery, UpdateOperation> builder = ImmutableMap.builder();
    for (DataQuery currentQuery : UpdateExpression.parseQuery(query, result)) {
      Optional<Number> value = data.get(currentQuery).flatMap(Coerce::asDouble);
      if (value.isEmpty()) {
        continue;
      }
      builder.put(
          currentQuery,
          UpdateOperation.replace(currentQuery, this.operator.apply(argNumber, value.get())));
    }

    return UpdateOperation.common(builder.build());
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, argNumber);
  }
}
