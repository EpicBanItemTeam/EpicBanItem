package team.ebi.epicbanitem.expression.predicate;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;

public class NePredicateExpression implements PredicateExpression {
  private final DataView value;

  public NePredicateExpression(DataView data) {
    this.value = data;
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return TestResult.from(!value.equals(data.getView(query).orElse(null)));
  }
}
