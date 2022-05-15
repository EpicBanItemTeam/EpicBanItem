package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
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
    var builder = ImmutableMap.<DataQuery, UpdateOperation>builder();
    for (DataQuery currentQuery : UpdateExpression.parseQuery(query, result)) {
      var list =
          data.getList(currentQuery)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          MessageFormat.format("$pop failed, {0} is invalid array", currentQuery)));
      if (value == Position.FIRST) {
        list.remove(0);
      } else if (value == Position.LAST) {
        list.remove(list.size() - 1);
      }
      builder.put(currentQuery, UpdateOperation.replace(currentQuery, list));
    }
    return UpdateOperation.common(builder.build());
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, value);
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
