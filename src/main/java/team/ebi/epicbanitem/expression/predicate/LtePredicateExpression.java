package team.ebi.epicbanitem.expression.predicate;

import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.expression.ComparePredicateExpression;

public class LtePredicateExpression extends ComparePredicateExpression {

  public LtePredicateExpression(DataView data) {
    super(data, (i, j) -> i <= j);
  }
}
