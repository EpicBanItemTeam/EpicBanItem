package team.ebi.epicbanitem.expression.query;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;
import team.ebi.epicbanitem.expression.ValueQueryExpression;
import team.ebi.epicbanitem.util.data.DataUtils;

/**
 * <code>
 * $all: [ { $elemMatch: {}}, "foo" ]
 * </code>
 */
public class AllQueryExpression implements QueryExpression {

  private final Set<QueryExpression> expressions;

  public AllQueryExpression(Set<QueryExpression> expressions) {
    this.expressions = expressions;
  }

  public AllQueryExpression(DataView data, DataQuery query) {
    List<?> values =
        data.getList(query)
            .orElseThrow(() -> new InvalidDataException("$all should be an array"));
    this.expressions =
        values.stream()
            .map(
                value -> {
                  if (value instanceof DataView view) {
                    return new CommonQueryExpression(view);
                  } else {
                    return new ValueQueryExpression(value);
                  }
                })
            .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    Optional<List<?>> list = DataUtils.get(data, query).flatMap(DataUtils::asList);
    if (list.isPresent() && !list.get().isEmpty()) {
      ImmutableMap.Builder<String, QueryResult> builder = ImmutableMap.builder();

      for (int i = 0; i < list.get().size(); i++) {
        String key = Integer.toString(i);
        DataQuery subQuery = query.then(key);
        expressions.stream()
            .map(it -> it.query(subQuery, data))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(it -> builder.put(key, it));
      }
      ImmutableMap<String, QueryResult> map = builder.build();
      if (map.size() >= expressions.size()) {
        return Optional.of(QueryResult.array(map));
      }
    }
    return QueryResult.failed();
  }


  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, expressions);
  }
}
