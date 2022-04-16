package team.ebi.epicbanitem.expression.predicate;

import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.expression.ComparePredicateExpression;

public class LtPredicateExpression extends ComparePredicateExpression {

  public LtPredicateExpression(DataView data) {
    super(data, (i, j) -> i < j);
  }
}
