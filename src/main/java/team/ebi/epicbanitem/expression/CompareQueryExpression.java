package team.ebi.epicbanitem.expression;

import com.google.common.base.Predicates;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataUtils;

public abstract class CompareQueryExpression implements QueryExpression {

  private static final Predicate<Object> IS_NUMBER = Predicates.instanceOf(Number.class);
  private final double value;
  private final BiPredicate<Double, Double> predicate;

  protected CompareQueryExpression(
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
        DataUtils.get(data, query)
            .filter(IS_NUMBER)
            .map(it -> this.predicate.test(((Number) it).doubleValue(), value))
            .orElse(false));
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, value);
  }
}
