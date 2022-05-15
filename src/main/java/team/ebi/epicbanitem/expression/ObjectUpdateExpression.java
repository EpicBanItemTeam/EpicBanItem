package team.ebi.epicbanitem.expression;

import com.google.common.collect.Maps;
import java.util.Map;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

/**
 * Wrapper for update expressions that value is an object for update multiple keys. Such as $set
 */
public class ObjectUpdateExpression implements UpdateExpression {

  private final Map<DataQuery, UpdateExpression> expressions;

  public ObjectUpdateExpression(
      UpdateExpressionFunction expressionProvider, DataView view, DataQuery query) {
    this.expressions = Maps.newHashMap();
    for (DataQuery subQuery :
        view.getView(query)
            .orElseThrow(() -> new InvalidDataException(query + "should be a object"))
            .keys(false)) {
      DataQuery entireQuery = query.then(subQuery);
      expressions.put(entireQuery, expressionProvider.apply(view, entireQuery));
    }
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    for (UpdateExpression expression : this.expressions.values()) {
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
