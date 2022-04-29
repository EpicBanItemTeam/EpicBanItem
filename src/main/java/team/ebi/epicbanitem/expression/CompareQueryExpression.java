package team.ebi.epicbanitem.expression;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class CompareQueryExpression implements QueryExpression {

  private final double value;
  private final BiPredicate<Double, Double> predicate;

  private static final Predicate<Object> IS_NUMBER = it -> it instanceof Number;

  public CompareQueryExpression(DataView data, BiPredicate<Double, Double> predicate) {
    this.value =
        data.get(DataQuery.of())
            .filter(IS_NUMBER)
            .map(it -> ((Number) it).doubleValue())
            .orElseThrow(InvalidDataException::new);
    this.predicate = predicate;
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    boolean result;
    if (data instanceof DataView) {
      result =
          ((DataView) data)
              .get(query)
              .filter(IS_NUMBER)
              .map(it -> this.predicate.test(value, ((Number) it).doubleValue()))
              .orElse(false);
    } else {
      result = data instanceof Number && predicate.test(((Number) data).doubleValue(), value);
    }
    return QueryResult.from(result);
  }
}
