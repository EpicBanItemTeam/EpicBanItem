package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.util.DataPreconditions;

public class ExistsQueryExpression implements QueryExpression {
  private static final String exception =
      "$exists should be one of 0, 1, true or false. Current: %s";
  private final boolean expect;

  public ExistsQueryExpression(DataView data) {
    Object value =
        data.get(DataQuery.of())
            .orElseThrow(() -> new InvalidDataException(String.format(exception, "null")));
    if (value instanceof Integer) {
      int i = (int) value;
      expect = i == 1;
      DataPreconditions.checkData(i == 0 || i == 1, exception, i);
    } else if (value instanceof Boolean) {
      expect = (boolean) value;
    } else throw new InvalidDataException(String.format(exception, value));
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(data.contains(query) == expect);
  }
}
