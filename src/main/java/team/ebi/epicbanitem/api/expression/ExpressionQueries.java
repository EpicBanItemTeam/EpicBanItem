package team.ebi.epicbanitem.api.expression;

import org.spongepowered.api.data.persistence.DataQuery;

public class ExpressionQueries {
  public static final DataQuery OR = DataQuery.of(ExpressionKeys.OR);
  public static final DataQuery NOR = DataQuery.of(ExpressionKeys.NOR);
  public static final DataQuery AND = DataQuery.of(ExpressionKeys.AND);
  public static final DataQuery NOT = DataQuery.of(ExpressionKeys.NOT);

  public static final DataQuery EQ = DataQuery.of(ExpressionKeys.EQ);
  public static final DataQuery NE = DataQuery.of(ExpressionKeys.NE);

  public static final DataQuery GT = DataQuery.of(ExpressionKeys.GT);
  public static final DataQuery LT = DataQuery.of(ExpressionKeys.LT);
  public static final DataQuery GTE = DataQuery.of(ExpressionKeys.GTE);
  public static final DataQuery LTE = DataQuery.of(ExpressionKeys.LTE);

  public static final DataQuery IN = DataQuery.of(ExpressionKeys.IN);
  public static final DataQuery NIN = DataQuery.of(ExpressionKeys.NIN);

  public static final DataQuery SIZE = DataQuery.of(ExpressionKeys.SIZE);
  public static final DataQuery ALL = DataQuery.of(ExpressionKeys.ALL);
  public static final DataQuery ELEM_MATCH = DataQuery.of(ExpressionKeys.ELEM_MATCH);

  public static final DataQuery EXISTS = DataQuery.of(ExpressionKeys.EXISTS);
  public static final DataQuery REGEX = DataQuery.of(ExpressionKeys.REGEX);
}
