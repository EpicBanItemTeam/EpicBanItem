package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.util.Tuple;
import team.ebi.epicbanitem.api.expression.QueryResult.Type;
import team.ebi.epicbanitem.util.DataPreconditions;

@FunctionalInterface
public interface UpdateExpression {

  /**
   * @param result The result from test expression
   * @param data {@link DataView} to modify
   * @return result
   */
  UpdateOperation update(QueryResult result, DataView data);

  static List<DataQuery> parseQuery(DataQuery query, QueryResult result) {
    Stream<Tuple<ImmutableList<String>, Optional<QueryResult>>> stream =
        Stream.of(new Tuple<>(ImmutableList.of(), Optional.of(result)));
    for (String part : query.parts()) {
      switch (part) {
        case "$":
          stream =
              stream.flatMap(
                  tuple -> {
                    ImmutableList<String> currentParts = tuple.first();
                    Optional<QueryResult> currentResult = tuple.second();
                    DataPreconditions.checkData(
                        currentResult.isPresent() && currentResult.get().type() != Type.ARRAY,
                        "Can't match \"$\" in %s, parent should be array",
                        String.join(".", currentParts));
                    return Stream.of(currentResult.get().children().entrySet().stream().findFirst())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(
                            it ->
                                Tuple.of(
                                    ImmutableList.<String>builder()
                                        .addAll(currentParts)
                                        .add(it.getKey())
                                        .build(),
                                    Optional.of(it.getValue())));
                  });
          break;
        case "$[]":
          stream =
              stream.flatMap(
                  tuple -> {
                    ImmutableList<String> currentParts = tuple.first();
                    Optional<QueryResult> currentResult = tuple.second();
                    DataPreconditions.checkData(
                        currentResult.isPresent() && currentResult.get().type() != Type.ARRAY,
                        "Can't match \"$[]\" in %s, parent should be array",
                        String.join(".", currentParts));
                    return currentResult.get().children().entrySet().stream()
                        .map(
                            it ->
                                Tuple.of(
                                    ImmutableList.<String>builder()
                                        .addAll(currentParts)
                                        .add(it.getKey())
                                        .build(),
                                    Optional.of(it.getValue())));
                  });
          break;
        default:
          stream =
              stream.map(
                  tuple ->
                      Tuple.of(
                          ImmutableList.<String>builder().addAll(tuple.first()).add(part).build(),
                          tuple.second().map(it -> it.children().get(part))));
      }
    }

    //noinspection UnstableApiUsage
    return stream.map(it -> DataQuery.of(it.first())).collect(ImmutableList.toImmutableList());
  }
}
