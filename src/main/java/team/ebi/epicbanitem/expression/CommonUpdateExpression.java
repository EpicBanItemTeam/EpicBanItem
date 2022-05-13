package team.ebi.epicbanitem.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionQueries.UPDATE_EXPRESSIONS;
import static team.ebi.epicbanitem.api.expression.UpdateExpressionFunctions.EXPRESSIONS;

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

  public CommonUpdateExpression(DataView view, DataQuery query) {
    this.expressions = Sets.newHashSet();
    for (DataQuery subQuery : view.keys(false)) {
      String key = subQuery.toString();
      if (UPDATE_EXPRESSIONS.contains(subQuery)) {
        this.expressions.add(EXPRESSIONS.get(key).apply(view, query.then(subQuery)));
        continue;
      }
      this.expressions.clear();
      break;
    }
    if (this.expressions.isEmpty())
      this.expressions.add(new ObjectUpdateExpression(SetUpdateExpression::new, view, query));
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation operation = UpdateOperation.common();
    for (UpdateExpression expression : expressions)
      operation = operation.merge(expression.update(result, data));
    return operation;
  }
}
