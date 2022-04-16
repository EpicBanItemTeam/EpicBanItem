package team.ebi.epicbanitem.api.expression;

import org.spongepowered.api.data.persistence.DataQuery;

public class PredicateExpressionQuerys {
  public static final DataQuery OR = DataQuery.of(PredicateExpressionKeys.OR);
  public static final DataQuery NOR = DataQuery.of(PredicateExpressionKeys.NOR);
  public static final DataQuery AND = DataQuery.of(PredicateExpressionKeys.AND);
  public static final DataQuery NOT = DataQuery.of(PredicateExpressionKeys.NOT);

  public static final DataQuery EQ = DataQuery.of(PredicateExpressionKeys.EQ);
  public static final DataQuery NE = DataQuery.of(PredicateExpressionKeys.NE);

  public static final DataQuery GT = DataQuery.of(PredicateExpressionKeys.GT);
  public static final DataQuery LT = DataQuery.of(PredicateExpressionKeys.LT);
  public static final DataQuery GTE = DataQuery.of(PredicateExpressionKeys.GTE);
  public static final DataQuery LTE = DataQuery.of(PredicateExpressionKeys.LTE);

  public static final DataQuery IN = DataQuery.of(PredicateExpressionKeys.IN);
  public static final DataQuery NIN = DataQuery.of(PredicateExpressionKeys.NIN);

  public static final DataQuery SIZE = DataQuery.of(PredicateExpressionKeys.SIZE);
  public static final DataQuery ALL = DataQuery.of(PredicateExpressionKeys.ALL);
  public static final DataQuery ELEM_MATCH = DataQuery.of(PredicateExpressionKeys.ELEM_MATCH);

  public static final DataQuery EXISTS = DataQuery.of(PredicateExpressionKeys.EXISTS);
  public static final DataQuery REGEX = DataQuery.of(PredicateExpressionKeys.REGEX);
}
