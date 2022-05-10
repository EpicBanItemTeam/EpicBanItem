package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.text.MessageFormat;
import org.spongepowered.api.data.persistence.DataContainer;
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

  public ObjectUpdateExpression(UpdateExpressionFunction expressionProvider, DataView value) {
    Builder<UpdateExpression> builder = ImmutableSet.builder();
    for (DataQuery query : value.keys(false)) {
      DataView view =
          value
              .getView(query)
              .orElseThrow(
                  () -> new InvalidDataException(MessageFormat.format("Can't find {0}", query)));
      String key = query.last().toString();
      builder.add(expressionProvider.apply(DataContainer.createNew().set(DataQuery.of(key), view)));
    }
    this.expressions = builder.build();
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    UpdateOperation updateOperation = UpdateOperation.common();
    for (UpdateExpression expression : this.expressions) {
      updateOperation.merge(expression.update(result, data));
    }
    return updateOperation;
  }
}
