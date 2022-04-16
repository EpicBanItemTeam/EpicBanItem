package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.PredicateExpressionKeys.ROOT_EXPRESSIONS;
import static team.ebi.epicbanitem.api.expression.PredicateExpressions.EXPRESSIONS;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.predicate.ExtraQueryPredicateExpression;

public class CommonPredicateExpression implements PredicateExpression {
  private final Set<PredicateExpression> expressions;

  public CommonPredicateExpression(DataView view) {
    this.expressions = Sets.newHashSet();
    for (DataQuery query : view.keys(false)) {
      String key = query.last().toString();
      //noinspection OptionalGetWithoutIsPresent
      DataView currentView = view.getView(query).get();
      if (ROOT_EXPRESSIONS.contains(key)) {
        this.expressions.add(EXPRESSIONS.get(key).apply(currentView));
        continue;
      }
      Optional<String> stringValue = view.getString(query);
      // {"foo.bar": "back"}, {"foo.bar": "/.*/"}
      if (stringValue.isPresent()) {
        this.expressions.add(new StringPredicateExpression(currentView));
        continue;
      }
      // "foo.bar": { $exp: "foo" }
      for (DataQuery subQuery : currentView.keys(false)) {
        String subKey = subQuery.last().toString();
        //noinspection OptionalGetWithoutIsPresent
        DataView subView = currentView.getView(subQuery).get();
        if (EXPRESSIONS.containsKey(subKey)) {
          this.expressions.add(
              new ExtraQueryPredicateExpression(EXPRESSIONS.get(subKey).apply(subView), query));
        }
      }
    }
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    TestResult result = TestResult.success();
    for (PredicateExpression expression : this.expressions) {
      Optional<TestResult> currentResult = expression.test(query, data);
      if (currentResult.isPresent()) {
        result = currentResult.get().merge(result);
      } else {
        return TestResult.failed();
      }
    }
    return Optional.of(result);
  }
}
