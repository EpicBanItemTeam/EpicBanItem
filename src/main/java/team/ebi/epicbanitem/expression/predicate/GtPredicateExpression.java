package team.ebi.epicbanitem.expression.predicate;

import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.expression.ComparePredicateExpression;

public class GtPredicateExpression extends ComparePredicateExpression {

  public GtPredicateExpression(DataView data) {
    super(data, (i, j) -> i > j);
  }
}
