package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionQueries.ROOT_QUERY_EXPRESSIONS;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunctions;
import team.ebi.epicbanitem.api.expression.QueryResult;

public record CommonQueryExpression(Map<DataQuery, QueryExpression> expressions) implements
    QueryExpression {

  public CommonQueryExpression(DataView view) {
    this(view, DataQuery.of());
  }

  public CommonQueryExpression(DataView view, DataQuery query) {
    this(Maps.newHashMap());
    Optional<DataView> currentView = view.getView(query);

    // Shouldn't be value
    if (currentView.isEmpty()) {
      return;
    }

    // Can be root expressions or nbt path
    currentView
        .get()
        .values(false)
        .forEach(
            (key, value) -> {
              DataQuery currentQuery = query.then(key);
              if (ROOT_QUERY_EXPRESSIONS.contains(key)) {
                this.expressions.put(currentQuery,
                    QueryExpressionFunctions.expressions.get(key.toString()).apply(view, currentQuery));
              } else if (!(value instanceof DataView subView)) {
                this.expressions.put(currentQuery,
                    new ExtraQueryQueryExpression(
                        new ValueQueryExpression(
                            view.get(currentQuery)
                                .orElseThrow(() -> new InvalidDataException("No value in view"))),
                        DataQuery.of('.', key.toString())));
              } else {
                for (DataQuery subQuery : subView.keys(false)) {
                  String expressionKey = subQuery.toString();
                  if (QueryExpressionFunctions.expressions.containsKey(expressionKey)) {
                    this.expressions.put(currentQuery.then(subQuery),
                        new ExtraQueryQueryExpression(
                            QueryExpressionFunctions.expressions
                                .get(expressionKey)
                                .apply(view, currentQuery.then(subQuery)),
                            DataQuery.of('.', key.toString())));
                  }
                }
              }
            });
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    QueryResult result = QueryResult.success();
    for (QueryExpression expression : this.expressions.values()) {
      Optional<QueryResult> currentResult = expression.query(query, data);
      if (currentResult.isPresent()) {
        result = currentResult.get().merge(result);
      } else {
        return QueryResult.failed();
      }

    }
    return Optional.of(result);
  }

  @Override
  public DataContainer toContainer() {
    DataContainer container = DataContainer.createNew();
    this.expressions.forEach((query, expression) -> container.set(query,
        expression.toContainer().get(ROOT).orElse(expression.toContainer())));
    return container;
  }
}
