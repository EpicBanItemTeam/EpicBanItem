package team.ebi.epicbanitem.expression.query;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;
import team.ebi.epicbanitem.expression.ValueQueryExpression;
import team.ebi.epicbanitem.util.DataUtils;

/**
 * <code>
 *   $all: [
 *    { $elemMatch: {}},
 *    "foo"
 *   ]
 * </code>
 */
public class AllQueryExpression implements QueryExpression {
  private final Set<QueryExpression> expressions;

  public AllQueryExpression(DataView data, DataQuery query) {
    List<DataView> views =
        data.getViewList(query)
            .orElseThrow(() -> new InvalidDataException("$all should be objects array"));
    this.expressions =
        views.stream()
            .map(
                view -> {
                  Optional<DataView> elemMatchView = view.getView(ExpressionQueries.ELEM_MATCH);
                  if (elemMatchView.isPresent()) {
                    // [{ $elemMatch: {} }]
                    return new CommonQueryExpression(data, elemMatchView.get().currentPath());
                  } else {
                    return new ValueQueryExpression(
                        view.getString(DataQuery.of())
                            .orElseThrow(
                                () ->
                                    new InvalidDataException(
                                        "$all should be string, regex or elemMatch")));
                  }
                })
            .collect(Collectors.toSet());
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    Optional<List<?>> list =
        DataUtils.get(data, query).filter(it -> it instanceof List).map(it -> (List<?>) it);
    if (list.isPresent() && !list.get().isEmpty()) {
      ImmutableMap.Builder<String, QueryResult> builder = ImmutableMap.builder();
      for (int i = 0; i < list.get().size(); i++) {
        String key = Integer.toString(i);
        DataQuery subQuery = query.then(key);
        expressions.stream()
            .map(it -> it.query(subQuery, data))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny()
            .ifPresent(it -> builder.put(key, it));
      }
      ImmutableMap<String, QueryResult> map = builder.build();
      if (!map.isEmpty()) return Optional.of(QueryResult.array(map));
    }
    return QueryResult.failed();
  }
}
