package team.ebi.epicbanitem.expression.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.ValueQueryExpression;

public class InQueryExpression implements QueryExpression {

  private final Set<QueryExpression> expressions;

  public InQueryExpression(DataView data, DataQuery query) {
    List<?> values =
        data.getList(query).orElseThrow(() -> new InvalidDataException("$in should be objects array"));
    this.expressions = values.stream().map(ValueQueryExpression::new).collect(Collectors.toSet());
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return expressions.stream()
        .map(it -> it.query(query, data))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findAny();
  }
}
