package team.ebi.epicbanitem.api.expression;

import org.spongepowered.api.data.persistence.DataQuery;

public class ExpressionQueries {
  public static final DataQuery OR = ExpressionKeys.query(ExpressionKeys.OR);
  public static final DataQuery NOR = ExpressionKeys.query(ExpressionKeys.NOR);
  public static final DataQuery AND = ExpressionKeys.query(ExpressionKeys.AND);
  public static final DataQuery NOT = ExpressionKeys.query(ExpressionKeys.NOT);

  public static final DataQuery EQ = ExpressionKeys.query(ExpressionKeys.EQ);
  public static final DataQuery NE = ExpressionKeys.query(ExpressionKeys.NE);

  public static final DataQuery GT = ExpressionKeys.query(ExpressionKeys.GT);
  public static final DataQuery LT = ExpressionKeys.query(ExpressionKeys.LT);
  public static final DataQuery GTE = ExpressionKeys.query(ExpressionKeys.GTE);
  public static final DataQuery LTE = ExpressionKeys.query(ExpressionKeys.LTE);

  public static final DataQuery IN = ExpressionKeys.query(ExpressionKeys.IN);
  public static final DataQuery NIN = ExpressionKeys.query(ExpressionKeys.NIN);

  public static final DataQuery SIZE = ExpressionKeys.query(ExpressionKeys.SIZE);
  public static final DataQuery ALL = ExpressionKeys.query(ExpressionKeys.ALL);
  public static final DataQuery ELEM_MATCH = ExpressionKeys.query(ExpressionKeys.ELEM_MATCH);

  public static final DataQuery EXISTS = ExpressionKeys.query(ExpressionKeys.EXISTS);
  public static final DataQuery REGEX = ExpressionKeys.query(ExpressionKeys.REGEX);

  // Update
  public static final DataQuery SET = ExpressionKeys.query(ExpressionKeys.SET);
  public static final DataQuery UNSET = ExpressionKeys.query(ExpressionKeys.UNSET);
  public static final DataQuery RENAME = ExpressionKeys.query(ExpressionKeys.RENAME);

  public static final DataQuery POP = ExpressionKeys.query(ExpressionKeys.POP);
  public static final DataQuery PULL = ExpressionKeys.query(ExpressionKeys.PULL);

  public static final DataQuery INC = ExpressionKeys.query(ExpressionKeys.INC);
  public static final DataQuery MUL = ExpressionKeys.query(ExpressionKeys.MUL);
}
