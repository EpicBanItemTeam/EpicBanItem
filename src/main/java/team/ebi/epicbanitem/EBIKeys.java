package team.ebi.epicbanitem;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import team.ebi.epicbanitem.api.expression.QueryExpression;

public class EBIKeys {
  public static final Key<Value<QueryExpression>> LAST_QUERY =
      Key.from(EpicBanItem.key("last_query"), QueryExpression.class);
}
