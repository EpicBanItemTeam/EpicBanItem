package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionKeys.ROOT_QUERY_EXPRESSIONS;
import static team.ebi.epicbanitem.api.expression.QueryExpressions.EXPRESSIONS;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class CommonQueryExpression implements QueryExpression {
  private final Set<QueryExpression> expressions;

  public CommonQueryExpression(DataView view) {
    this.expressions = Sets.newHashSet();
    if (view.keys(false).isEmpty()) {
      Optional<DataView> currentView = view.getView(DataQuery.of());
      if (!currentView.isPresent()) return;
      Optional<String> stringValue = currentView.get().getString(DataQuery.of());
      if (!stringValue.isPresent()) return;
      this.expressions.add(new StringQueryExpression(stringValue.get()));
      return;
    }
    for (DataQuery query : view.keys(false)) {
      String key = query.last().toString();
      //noinspection OptionalGetWithoutIsPresent
      DataView currentView = view.getView(query).get();
      if (ROOT_QUERY_EXPRESSIONS.contains(key)) {
        this.expressions.add(EXPRESSIONS.get(key).apply(currentView));
        continue;
      }
      Optional<String> stringValue = view.getString(query);
      // {"foo.bar": "back"}, {"foo.bar": "/.*/"}
      if (stringValue.isPresent()) {
        this.expressions.add(new StringQueryExpression(stringValue.get()));
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
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    QueryResult result = QueryResult.success();
    for (QueryExpression expression : this.expressions) {
      Optional<QueryResult> currentResult = expression.query(query, data);
      if (currentResult.isPresent()) {
        result = currentResult.get().merge(result);
      } else {
        return QueryResult.failed();
      }
    }
    return Optional.of(result);
  }
}
