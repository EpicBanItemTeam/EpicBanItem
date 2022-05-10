package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Tuple;
import team.ebi.epicbanitem.api.expression.QueryResult.Type;

@FunctionalInterface
public interface UpdateExpression {

  /**
   * @param result The result from test expression
   * @param data {@link DataView} to modify
   * @return result
   */
  UpdateOperation update(QueryResult result, DataView data);

  static List<DataQuery> parseQuery(DataQuery query, QueryResult result) {
    Stream<Tuple<DataQuery, Optional<QueryResult>>> stream =
        Stream.of(new Tuple<>(DataQuery.of(), Optional.of(result)));
    for (String part : query.parts()) {
      switch (part) {
        case "$":
          stream =
              stream.flatMap(
                  tuple -> {
                    DataQuery current = tuple.first();
                    QueryResult subResult =
                        tuple
                            .second()
                            .filter(it -> it.type() == Type.ARRAY)
                            .orElseThrow(
                                () ->
                                    new InvalidDataException(
                                        MessageFormat.format(
                                            "Can't match \"$\" in {0}, parent should be array",
                                            current)));
                    return Stream.of(
                            subResult.entrySet().stream()
                                .findFirst()
                                .map(
                                    it ->
                                        Tuple.of(
                                            current.then(it.getKey()), Optional.of(it.getValue()))))
                        .filter(Optional::isPresent)
                        .map(Optional::get);
                  });
          break;
        case "$[]":
          stream =
              stream.flatMap(
                  tuple -> {
                    DataQuery current = tuple.first();
                    QueryResult subResult =
                        tuple
                            .second()
                            .filter(it -> it.type() == Type.ARRAY)
                            .orElseThrow(
                                () ->
                                    new InvalidDataException(
                                        MessageFormat.format(
                                            "Can't match \"$[]\" in {0}, parent should be array",
                                            current)));
                    return subResult.entrySet().stream()
                        .map(it -> Tuple.of(current.then(it.getKey()), Optional.of(it.getValue())));
                  });
          break;
        default:
          stream =
              stream.map(
                  tuple ->
                      Tuple.of(tuple.first().then(part), tuple.second().map(it -> it.get(part))));
      }
    }

    //noinspection UnstableApiUsage
    return stream.map(Tuple::first).collect(ImmutableList.toImmutableList());
  }
}
