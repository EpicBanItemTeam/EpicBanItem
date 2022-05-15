package team.ebi.epicbanitem.expression.query;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class AndQueryExpression implements QueryExpression {

  private final ImmutableList<QueryExpression> expressions;

  public AndQueryExpression(List<QueryExpression> expressions) {
    this.expressions = ImmutableList.copyOf(expressions);
  }

  @SuppressWarnings("UnstableApiUsage")
  public AndQueryExpression(DataView data, DataQuery query) {
    List<DataView> views =
        data.getViewList(query)
            .orElseThrow(() -> new InvalidDataException("$and should be an object array"));
    this.expressions =
        views.stream()
            .map(it -> new CommonQueryExpression(data, it.currentPath()))
            .collect(ImmutableList.toImmutableList());
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    QueryResult result = QueryResult.success();
    for (QueryExpression expression : this.expressions) {
      Optional<QueryResult> currentResult = expression.query(query, data);
      if (currentResult.isPresent()) result = result.merge(currentResult.get());
      else return QueryResult.failed();
    }
    return Optional.of(result);
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, expressions);
  }
}
