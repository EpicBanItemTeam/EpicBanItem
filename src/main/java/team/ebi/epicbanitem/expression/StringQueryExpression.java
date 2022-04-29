package team.ebi.epicbanitem.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.query.EqQueryExpression;
import team.ebi.epicbanitem.expression.query.RegexQueryExpression;
import team.ebi.epicbanitem.util.Regex;

public class StringQueryExpression implements QueryExpression {

  private final QueryExpression expression;

  public StringQueryExpression(String value) {
    this.expression =
        Regex.isRegex(value)
            ? new RegexQueryExpression(value)
            : new ArrayableQueryExpression(new EqQueryExpression(value));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    return expression.query(query, data);
  }
}
