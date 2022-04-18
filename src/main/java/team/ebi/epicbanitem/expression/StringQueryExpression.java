package team.ebi.epicbanitem.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.query.EqQueryExpression;
import team.ebi.epicbanitem.expression.query.RegexQueryExpression;
import team.ebi.epicbanitem.util.Regex;

public class StringQueryExpression implements QueryExpression {

  private final QueryExpression expression;

  public StringQueryExpression(DataView data) {
    String value = data.getString(DataQuery.of()).orElseThrow(InvalidDataException::new);
    this.expression =
        Regex.isRegex(value)
            ? new RegexQueryExpression(data)
            : new ArrayableQueryExpression(new EqQueryExpression(data));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return expression.query(query, data);
  }
}
