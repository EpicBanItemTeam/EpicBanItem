package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import java.util.regex.Pattern;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataUtils;
import team.ebi.epicbanitem.util.Regex;

public class RegexQueryExpression implements QueryExpression {
  private final Pattern pattern;

  public RegexQueryExpression(String value) {
    Regex regex = new Regex(value);
    this.pattern = regex.pattern();
  }

  public RegexQueryExpression(DataView view, DataQuery query) {
    this(view.getString(query)
        .orElseThrow(
            () ->
                new InvalidDataException(
                    "$regex should be string")));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(
        DataUtils.get(data, query)
            .filter(it -> it instanceof String)
            .map(it -> (String) it)
            .filter(this.pattern.asPredicate())
            .isPresent());
  }
}
