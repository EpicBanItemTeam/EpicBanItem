package team.ebi.epicbanitem.expression.query;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;

public class EqQueryExpression implements QueryExpression {
  private final DataView value;

  public EqQueryExpression(DataView data) {
    this.value = data;
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(value.equals(data.getView(query).orElse(null)));
  }
}
