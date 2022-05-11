package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class PopUpdateExpression implements UpdateExpression {

  private final DataQuery query;
  private final Position value;

  public PopUpdateExpression(DataView view, DataQuery query) {
    this.query = DataQuery.of('.', query.last().toString());
    this.value =
        Position.fromId(
            view.getInt(query)
                .filter(it -> Math.abs(it) == 1)
                .orElseThrow(() -> new InvalidDataException(query + "need 1 or -1")));
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    ImmutableMap.Builder<DataQuery, UpdateOperation> builder = ImmutableMap.builder();
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      List<?> list =
          data.getList(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("$pop failed, {0} is invalid array", query)));
      switch (value) {
        case FIRST:
          list.remove(0);
          break;
        case LAST:
          list.remove(list.size() - 1);
          break;
      }
      builder.put(query, UpdateOperation.replace(query, list));
    }
    return UpdateOperation.common(builder.build());
  }

  enum Position {
    FIRST(-1),
    LAST(1);

    private static final ImmutableMap<Integer, Position> BY_ID =
        ImmutableMap.<Integer, Position>builder()
            .putAll(
                Arrays.stream(Position.values())
                    .collect(
                        Collectors.<Position, Integer, Position>toMap(
                            it -> it.id, Function.identity())))
            .build();

    public final int id;

    Position(int id) {
      this.id = id;
    }

    static Position fromId(int id) {
      return BY_ID.get(id);
    }
  }
}
