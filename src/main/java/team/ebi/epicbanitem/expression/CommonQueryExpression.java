package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionKeys.ROOT_EXPRESSIONS;
import static team.ebi.epicbanitem.api.expression.QueryExpressions.EXPRESSIONS;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.query.ExtraQueryQueryExpression;

public class CommonQueryExpression implements QueryExpression {
  private final Set<QueryExpression> expressions;

  public CommonQueryExpression(DataView view) {
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
        this.expressions.add(new StringQueryExpression(currentView));
        continue;
      }
      // "foo.bar": { $exp: "foo" }
      for (DataQuery subQuery : currentView.keys(false)) {
        String subKey = subQuery.last().toString();
        //noinspection OptionalGetWithoutIsPresent
        DataView subView = currentView.getView(subQuery).get();
        if (EXPRESSIONS.containsKey(subKey)) {
          this.expressions.add(
              new ExtraQueryQueryExpression(EXPRESSIONS.get(subKey).apply(subView), query));
        }
      }
    }
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    TestResult result = TestResult.success();
    for (QueryExpression expression : this.expressions) {
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
