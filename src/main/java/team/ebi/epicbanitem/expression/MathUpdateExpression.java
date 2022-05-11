package team.ebi.epicbanitem.expression;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.BinaryOperator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
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
            .filter(Predicates.instanceOf(Number.class))
            .map(it -> (Number) it)
            .orElseThrow(() -> new InvalidDataException(query + "need a number input"));
    this.operator = operator;
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    ImmutableMap.Builder<DataQuery, UpdateOperation> builder = ImmutableMap.builder();
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      Optional<Number> value =
          data.get(query).filter(Predicates.instanceOf(Number.class)).map(it -> (Number) it);
      if (value.isEmpty()) continue;
      builder.put(
          query,
          UpdateOperation.replace(query, this.operator.apply(argNumber, value.get())));
    }

    return UpdateOperation.common(builder.build());
  }
}
