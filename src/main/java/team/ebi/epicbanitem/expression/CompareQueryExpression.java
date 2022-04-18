package team.ebi.epicbanitem.expression;

import java.util.Optional;
import java.util.function.BiPredicate;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class CompareQueryExpression implements QueryExpression {

  private final int value;
  private final BiPredicate<Integer, Integer> predicate;

  public CompareQueryExpression(DataView data, BiPredicate<Integer, Integer> predicate) {
    this.value = data.getInt(DataQuery.of()).orElseThrow(InvalidDataException::new);
    this.predicate = predicate;
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(
        data.getInt(query).map(it -> this.predicate.test(value, it)).orElse(false));
  }
}
