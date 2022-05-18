package team.ebi.epicbanitem.expression.update;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.expression.MathUpdateExpression;

public class IncUpdateExpression extends MathUpdateExpression {

  public IncUpdateExpression(DataQuery query, Number argNumber) {
    super(query, argNumber, IncUpdateExpression::sum);
  }

  public IncUpdateExpression(DataView view, DataQuery query) {
    super(view, query, IncUpdateExpression::sum);
  }

  private static Number sum(Number source, Number arg) {
    if (source instanceof Byte) {
      return (byte) (source.byteValue() + arg.byteValue());
    }
    if (source instanceof Short) {
      return (short) (source.shortValue() + arg.shortValue());
    }
    if (source instanceof Integer) {
      return source.intValue() + arg.intValue();
    }
    if (source instanceof Long) {
      return source.longValue() + arg.longValue();
    }
    if (source instanceof Float) {
      return source.floatValue() + arg.floatValue();
    }
    if (source instanceof Double) {
      return source.doubleValue() + arg.doubleValue();
    }
    throw new InvalidDataException("Source isn't a number");
  }
}
