package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

/** Wrapper for update expressions that value is an object for update multiple keys. Such as $set */
public class ObjectUpdateExpression implements UpdateExpression {

  private final ImmutableSet<UpdateExpression> expressions;

  public ObjectUpdateExpression(
      UpdateExpressionFunction expressionProvider, DataView view, DataQuery query) {
    ImmutableSet.Builder<UpdateExpression> builder = ImmutableSet.builder();
    for (DataQuery subQuery :
        view.getView(query)
            .orElseThrow(() -> new InvalidDataException(query + "should be a object"))
            .keys(false)) {
      DataQuery entireQuery = query.then(subQuery);
      builder.add(expressionProvider.apply(view, entireQuery));
    }
    this.expressions = builder.build();
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    for (UpdateExpression expression : this.expressions)
      operation = operation.merge(expression.update(result, data));
    return operation;
  }
}
