package team.ebi.epicbanitem.expression;

import java.util.Optional;
import java.util.function.BiPredicate;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Coerce;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.data.DataUtils;

public abstract class CompareQueryExpression implements QueryExpression {
  private final double value;
  private final BiPredicate<Double, Double> predicate;

  protected CompareQueryExpression(double value, BiPredicate<Double, Double> predicate) {
    this.value = value;
    this.predicate = predicate;
  }

  protected CompareQueryExpression(
      DataView data, DataQuery query, BiPredicate<Double, Double> predicate) {
    this.value =
        data.get(query)
            .flatMap(Coerce::asDouble)
            .orElseThrow(InvalidDataException::new);
    this.predicate = predicate;
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(
        DataUtils.get(data, query)
            .flatMap(Coerce::asDouble)
            .map(it -> this.predicate.test(it, value))
            .orElse(false));
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, value);
  }
}
