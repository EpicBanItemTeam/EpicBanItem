package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.Sets;
import java.util.Set;

public class PredicateExpressionKeys {
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

  /**
   * The expressions can at root level
   */
  public static final Set<String> ROOT_EXPRESSIONS = Sets.newHashSet(OR, NOR, AND);
}
