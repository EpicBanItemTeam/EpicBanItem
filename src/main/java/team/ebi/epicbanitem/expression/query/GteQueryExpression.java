package team.ebi.epicbanitem.expression.query;

import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.expression.CompareQueryExpression;

public class GteQueryExpression extends CompareQueryExpression {

  public GteQueryExpression(DataView data) {
    super(data, (i, j) -> i >= j);
  }
}
