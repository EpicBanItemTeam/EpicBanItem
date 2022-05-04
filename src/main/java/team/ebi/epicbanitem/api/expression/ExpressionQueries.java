package team.ebi.epicbanitem.api.expression;

import static team.ebi.epicbanitem.api.expression.ExpressionKeys.query;

import com.google.common.collect.Sets;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataQuery;

public class ExpressionQueries {
  public static final DataQuery OR = query(ExpressionKeys.OR);
  public static final DataQuery NOR = query(ExpressionKeys.NOR);
  public static final DataQuery AND = query(ExpressionKeys.AND);
  public static final DataQuery NOT = query(ExpressionKeys.NOT);

  public static final DataQuery EQ = query(ExpressionKeys.EQ);
  public static final DataQuery NE = query(ExpressionKeys.NE);

  public static final DataQuery GT = query(ExpressionKeys.GT);
  public static final DataQuery LT = query(ExpressionKeys.LT);
  public static final DataQuery GTE = query(ExpressionKeys.GTE);
  public static final DataQuery LTE = query(ExpressionKeys.LTE);

  public static final DataQuery IN = query(ExpressionKeys.IN);
  public static final DataQuery NIN = query(ExpressionKeys.NIN);

  public static final DataQuery SIZE = query(ExpressionKeys.SIZE);
  public static final DataQuery ALL = query(ExpressionKeys.ALL);
  public static final DataQuery ELEM_MATCH = query(ExpressionKeys.ELEM_MATCH);

  public static final DataQuery EXISTS = query(ExpressionKeys.EXISTS);
  public static final DataQuery REGEX = query(ExpressionKeys.REGEX);

  // Update
  public static final DataQuery SET = query(ExpressionKeys.SET);
  public static final DataQuery UNSET = query(ExpressionKeys.UNSET);
  public static final DataQuery RENAME = query(ExpressionKeys.RENAME);

  public static final DataQuery POP = query(ExpressionKeys.POP);
  public static final DataQuery PULL = query(ExpressionKeys.PULL);

  public static final DataQuery INC = query(ExpressionKeys.INC);
  public static final DataQuery MUL = query(ExpressionKeys.MUL);

  /** The expressions can at root level */
  public static final Set<DataQuery> ROOT_QUERY_EXPRESSIONS = Sets.newHashSet(OR, NOR, AND);

  public static final Set<DataQuery> ARRAY_QUERY_EXPRESSIONS =
      Sets.newHashSet(IN, NIN, ALL, AND, OR, NOR);
}
