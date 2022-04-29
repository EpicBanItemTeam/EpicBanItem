package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import java.util.regex.Pattern;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.Regex;

public class RegexQueryExpression implements QueryExpression {
  private final Pattern pattern;

  public RegexQueryExpression(String value) {
    Regex regex = new Regex(value);
    this.pattern = regex.pattern();
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, Object data) {
    Optional<String> value =
        data instanceof DataView
            ? ((DataView) data).getString(DataQuery.of())
            : Optional.ofNullable(data == null ? null : data.toString());
    return QueryResult.from(value.filter(this.pattern.asPredicate()).isPresent());
  }
}
