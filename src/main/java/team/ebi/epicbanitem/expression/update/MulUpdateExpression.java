package team.ebi.epicbanitem.expression.update;

import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.expression.MathUpdateExpression;

public class MulUpdateExpression extends MathUpdateExpression {

  public MulUpdateExpression(DataView view) {
    super(view, MulUpdateExpression::mul);
  }

  private static Number mul(Number source, Number arg) {
    if (source instanceof Byte) return source.byteValue() * arg.byteValue();
    if (source instanceof Short) return source.shortValue() * arg.shortValue();
    if (source instanceof Integer) return source.intValue() * arg.intValue();
    if (source instanceof Long) return source.longValue() * arg.longValue();
    if (source instanceof Float) return source.floatValue() * arg.floatValue();
    if (source instanceof Double) return source.doubleValue() * arg.doubleValue();
    throw new InvalidDataException("Source isn't a number");
  }
}
