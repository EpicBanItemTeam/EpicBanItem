package team.ebi.epicbanitem.expression.update;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
  private final DataQuery first;
  private final Position value;

  public PopUpdateExpression(DataView data) {
    this.query = DataQuery.of('.', data.currentPath().toString());
    this.first = query.queryParts().get(0);
    Optional<Integer> input = data.getInt(DataQuery.of());
    this.value =
        Position.fromId(
            input
                .filter(Position.BY_ID::containsKey)
                .orElseThrow(
                    () ->
                        new InvalidDataException(
                            String.format("$pop has invalid input: %s", input.orElse(null)))));
  }

  @Override
  public @NotNull UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    DataContainer container = DataContainer.createNew();
    data.getView(first).ifPresent(it -> container.set(first, it));
    for (DataQuery query : UpdateExpression.parseQuery(query, result)) {
      DataView view =
          container
              .getView(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          String.format("$pop failed, %s is invalid", query)));
      List<DataView> views =
          view.getViewList(query)
              .orElseThrow(
                  () ->
                      new UnsupportedOperationException(
                          String.format("$pop failed, %s isn't an array", query)));
      switch (value) {
        case FIRST:
          views.remove(0);
          break;
        case LAST:
          views.remove(views.size() - 1);
          break;
      }
      view.set(DataQuery.of(), views);
      updateOperation = updateOperation.merge(UpdateOperation.replace(view));
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
