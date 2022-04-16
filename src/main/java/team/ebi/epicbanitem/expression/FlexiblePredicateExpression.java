package team.ebi.epicbanitem.expression;

import com.google.common.base.Suppliers;
import java.util.Optional;
import java.util.function.Supplier;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.predicate.ElemMatchPredicateExpression;

/** Use for wrap the expressions that can test for value and array */
public class FlexiblePredicateExpression implements PredicateExpression {
  private final Supplier<ElemMatchPredicateExpression> elemMatchExpression;
  private final PredicateExpression expression;

  public FlexiblePredicateExpression(PredicateExpression expression) {
    this.elemMatchExpression =
        Suppliers.memoize(() -> new ElemMatchPredicateExpression(expression));
    this.expression = expression;
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    Optional<TestResult> result = this.expression.test(query, data);
    return result.isPresent() ? result : this.elemMatchExpression.get().test(query, data);
  }
}
