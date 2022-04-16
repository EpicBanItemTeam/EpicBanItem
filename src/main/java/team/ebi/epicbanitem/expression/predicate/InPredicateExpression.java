package team.ebi.epicbanitem.expression.predicate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.StringPredicateExpression;

public class InPredicateExpression implements PredicateExpression {

  private final Set<PredicateExpression> expressions;

  public InPredicateExpression(DataView data) {
    List<DataView> views =
        data.getViewList(DataQuery.of())
            .orElseThrow(() -> new InvalidDataException("$in should be a array"));
    this.expressions =
        views.stream().map(StringPredicateExpression::new).collect(Collectors.toSet());
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return expressions.stream()
        .map(it -> it.test(query, data))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findAny();
  }
}
