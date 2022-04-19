package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.Sets;
import java.util.Set;

public class ExpressionKeys {
  // Query
  public static final String OR = "$or";
  public static final String NOR = "$nor";
  public static final String AND = "$and";
  public static final String NOT = "$not";

  public static final String EQ = "$eq";
  public static final String NE = "$ne";

  public static final String GT = "$gt";
  public static final String LT = "$lt";
  public static final String GTE = "$gte";
  public static final String LTE = "$lte";

  public static final String IN = "$in";
  public static final String NIN = "$nin";

  public static final String SIZE = "$size";
  public static final String ALL = "$all";
  public static final String ELEM_MATCH = "$elemMatch";

  public static final String EXISTS = "$exists";
  public static final String REGEX = "$regex";

  // Update
  public static final String SET = "$set";
  public static final String UNSET = "$unset";
  public static final String RENAME = "$rename";
  public static final String POP = "$pop";
  public static final String PULL = "$pull";
  public static final String INC = "$ind";
  public static final String MUL = "$mul";

  /** The expressions can at root level */
  public static final Set<String> ROOT_QUERY_EXPRESSIONS = Sets.newHashSet(OR, NOR, AND);

  public static final Set<String> UPDATE_EXPRESSIONS =
      Sets.newHashSet(SET, UNSET, RENAME, POP, PULL, INC, MUL);
}
