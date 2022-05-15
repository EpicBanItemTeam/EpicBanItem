package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionQueries.UPDATE_EXPRESSIONS;

import com.google.common.collect.Maps;
import java.util.Map;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunctions;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.update.SetUpdateExpression;

public class CommonUpdateExpression implements UpdateExpression {

  private final Map<DataQuery, UpdateExpression> expressions;

  public CommonUpdateExpression(DataView view, DataQuery query) {
    this.expressions = Maps.newHashMap();
    for (DataQuery subQuery : view.keys(false)) {
      String key = subQuery.toString();
      DataQuery entireQuery = query.then(subQuery);
      if (UPDATE_EXPRESSIONS.contains(subQuery)) {
        this.expressions.put(entireQuery, UpdateExpressionFunctions.expressions.get(key).apply(view, entireQuery));
      } else {
        this.expressions.clear();
        break;
      }
    }
    if (this.expressions.isEmpty()) {
      this.expressions.put(query,
          new ObjectUpdateExpression(SetUpdateExpression::new, view, query));
    }
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    for (UpdateExpression expression : expressions.values()) {
      operation = operation.merge(expression.update(result, data));
    }
    return operation;
  }

  @Override
  public DataContainer toContainer() {
    DataContainer container = DataContainer.createNew();
    this.expressions.forEach((query, expression) -> container.set(query,
        expression.toContainer().get(ROOT).orElse(expression.toContainer())));
    return container;
  }
}
