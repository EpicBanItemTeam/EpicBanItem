package team.ebi.epicbanitem.expression.query;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.expression.CompareQueryExpression;

public class GtQueryExpression extends CompareQueryExpression {

  public GtQueryExpression(double value) {
    super(value, (i, j) -> i > j);
  }

  public GtQueryExpression(DataView data, DataQuery query) {
    super(data, query, (i, j) -> i > j);
  }
}
