package team.ebi.epicbanitem.expression;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataViewUtils;

public abstract class CompareQueryExpression implements QueryExpression {

  private final double value;
  private final BiPredicate<Double, Double> predicate;

  private static final Predicate<Object> IS_NUMBER = it -> it instanceof Number;

  public CompareQueryExpression(
      DataView data, DataQuery query, BiPredicate<Double, Double> predicate) {
    this.value =
        data.get(query)
            .filter(IS_NUMBER)
            .map(it -> ((Number) it).doubleValue())
            .orElseThrow(InvalidDataException::new);
    this.predicate = predicate;
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(
        DataViewUtils.get(data, query)
            .filter(IS_NUMBER)
            .map(it -> this.predicate.test(((Number) it).doubleValue(), value))
            .orElse(false));
  }
}
