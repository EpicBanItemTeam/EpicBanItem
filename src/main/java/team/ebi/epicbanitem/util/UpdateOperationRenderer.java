package team.ebi.epicbanitem.util;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UpdateOperationRenderer {
  public static ImmutableList<Component> render(UpdateOperation operation) {
    return ImmutableList.copyOf(operation.asComponent().children());
  }
}
