package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

  public PopUpdateExpression(DataView data) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    Optional<Integer> input = data.getInt(DataQuery.of());
    this.value =
        Position.fromId(
            input
                .filter(Position.BY_ID::containsKey)
                .orElseThrow(
                    () ->
                        new InvalidDataException(
                            MessageFormat.format(
                                "$pop has invalid input: {0}", input.orElse(null)))));
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
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
      updateOperation = updateOperation.merge(UpdateOperation.replace(query, list));
    }

    return updateOperation;
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
