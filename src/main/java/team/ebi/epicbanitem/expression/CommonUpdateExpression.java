package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionKeys.UPDATE_EXPRESSIONS;
import static team.ebi.epicbanitem.api.expression.UpdateExpressions.EXPRESSIONS;

import com.google.common.collect.Sets;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.update.SetUpdateExpression;

public class CommonUpdateExpression implements UpdateExpression {
  private final Set<UpdateExpression> expressions;

  public CommonUpdateExpression(DataView view) {
    this.expressions = Sets.newHashSet();
    for (DataQuery query : view.keys(false)) {
      String key = query.toString();
      //noinspection OptionalGetWithoutIsPresent
      DataView currentView = view.getView(query).get();
      if (UPDATE_EXPRESSIONS.contains(key)) {
        this.expressions.add(EXPRESSIONS.get(key).apply(currentView));
        continue;
      }
      this.expressions.clear();
      break;
    }
    if (this.expressions.isEmpty()) this.expressions.add(new SetUpdateExpression(view));
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    for (UpdateExpression expression : expressions)
      operation = operation.merge(expression.update(result, data));
    return operation;
  }
}
