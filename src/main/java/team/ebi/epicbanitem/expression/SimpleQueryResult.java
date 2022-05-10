package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class SimpleQueryResult extends AbstractMap<String, QueryResult> implements QueryResult {
  private final ImmutableMap<String, QueryResult> children;
  private final QueryResult.Type type;

  public SimpleQueryResult() {
    this(Type.DEFAULT);
  }

  public SimpleQueryResult(QueryResult.Type type) {
    this(type, Maps.newHashMap());
  }

  public SimpleQueryResult(QueryResult.Type type, Map<String, QueryResult> children) {
    this.children = ImmutableMap.copyOf(children);
    this.type = type;
  }

  @Override
  public QueryResult merge(@NotNull QueryResult target) {
    return new SimpleQueryResult(
        type.merge(target.type()),
        new LinkedHashMap<>(children) {
          {
            target.forEach(
                (key, value) ->
                    compute(
                        key,
                        (ignored, oldValue) ->
                            Objects.isNull(oldValue) ? value : oldValue.merge(value)));
          }
        });
  }

  @Override
  public QueryResult.Type type() {
    return type;
  }

  @Override
  public QueryResult get(Object key) {
    return children.get(key);
  }

  @NotNull
  @Override
  public Set<Entry<String, QueryResult>> entrySet() {
    return children.entrySet();
  }
}
