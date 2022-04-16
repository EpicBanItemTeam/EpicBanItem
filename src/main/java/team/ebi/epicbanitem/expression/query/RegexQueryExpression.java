package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import java.util.regex.Pattern;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.util.Regex;

public class RegexQueryExpression implements QueryExpression {
  private final Pattern pattern;

  public RegexQueryExpression(DataView data) {
    String s =
        data.getString(DataQuery.of())
            .orElseThrow(() -> new InvalidDataException("$regex should be a string"));
    Regex regex = new Regex(s);
    this.pattern = regex.pattern();
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(data.getString(query).filter(this.pattern.asPredicate()).isPresent());
  }
}
