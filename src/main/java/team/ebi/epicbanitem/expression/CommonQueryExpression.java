package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionQueries.ROOT_QUERY_EXPRESSIONS;
import static team.ebi.epicbanitem.api.expression.QueryExpressions.EXPRESSIONS;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class CommonQueryExpression implements QueryExpression {
  private final Set<QueryExpression> expressions;

  public CommonQueryExpression(DataView view, DataQuery query) {
    this.expressions = Sets.newHashSet();
    Optional<DataView> currentView = view.getView(query);

    // Should be value
    if (!currentView.isPresent()) {
      this.expressions.add(
          new ValueQueryExpression(
              view.get(query).orElseThrow(() -> new InvalidDataException("No value in view"))));
      return;
    }

    // Can be root expressions or nbt path
    Set<DataQuery> keys = currentView.get().keys(false);
    for (DataQuery key : keys) {
      Optional<DataView> subView = currentView.get().getView(key);

      if (ROOT_QUERY_EXPRESSIONS.contains(key)) {
        this.expressions.add(EXPRESSIONS.get(key.toString()).apply(view, query.then(key)));
        continue;
      }

      // Can be list or value
      if (!subView.isPresent()) {
        this.expressions.add(new ExtraQueryQueryExpression(new CommonQueryExpression(view, query.then(key)), DataQuery.of('.', key.toString())));
        continue;
      }

      // a.b: { $gt: 8, $lte: 12, $in: [5, 9, 10] }
      for (DataQuery subQuery : subView.get().keys(false)) {
        if (EXPRESSIONS.containsKey(subQuery.toString())) {
          this.expressions.add(
              new ExtraQueryQueryExpression(
                  EXPRESSIONS.get(subQuery.toString()).apply(view, query.then(key)),
                  DataQuery.of('.', key.toString())));
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
