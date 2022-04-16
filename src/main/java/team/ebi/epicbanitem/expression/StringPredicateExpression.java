package team.ebi.epicbanitem.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.predicate.EqPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.RegexPredicateExpression;
import team.ebi.epicbanitem.util.Regex;

public class StringPredicateExpression implements PredicateExpression {

  private final PredicateExpression expression;

  public StringPredicateExpression(DataView data) {
    String value = data.getString(DataQuery.of()).orElseThrow(InvalidDataException::new);
    this.expression =
        Regex.isRegex(value)
            ? new RegexPredicateExpression(data)
            : new FlexiblePredicateExpression(new EqPredicateExpression(data));
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return expression.test(query, data);
  }
}
