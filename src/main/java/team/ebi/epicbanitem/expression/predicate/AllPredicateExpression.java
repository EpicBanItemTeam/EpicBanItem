package team.ebi.epicbanitem.expression.predicate;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.PredicateExpressionQuerys;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonPredicateExpression;
import team.ebi.epicbanitem.expression.StringPredicateExpression;

/**
 * <code>
 *   $all: [
 *    { $elemMatch: {}},
 *    "foo"
 *   ]
 * </code>
 */
public class AllPredicateExpression implements PredicateExpression {
  private final Set<PredicateExpression> expressions;

  public AllPredicateExpression(DataView data) {
    List<DataView> views =
        data.getViewList(DataQuery.of())
            .orElseThrow(() -> new InvalidDataException("$all should be an array"));
    this.expressions =
        views.stream()
            .map(
                view -> {
                  Optional<DataView> elemMatchView =
                      view.getView(PredicateExpressionQuerys.ELEM_MATCH);
                  if (elemMatchView.isPresent()) {
                    // [{ $elemMatch: {} }]
                    return new CommonPredicateExpression(elemMatchView.get());
                  } else {
                    return new StringPredicateExpression(view);
                  }
                })
            .collect(Collectors.toSet());
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    Optional<List<DataView>> views = data.getViewList(DataQuery.of());
    if (views.isPresent() && !views.get().isEmpty()) {
      ImmutableMap.Builder<String, TestResult> builder = ImmutableMap.builder();
      for (int i = 0; i < views.get().size(); i++) {
        String key = Integer.toString(i);
        DataQuery subQuery = query.then(key);
        expressions.stream()
            .map(it -> it.test(subQuery, data))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny()
            .ifPresent(it -> builder.put(key, it));
      }
      ImmutableMap<String, TestResult> map = builder.build();
      if (!map.isEmpty()) return Optional.of(TestResult.array(map));
    }
    return TestResult.failed();
  }
}
