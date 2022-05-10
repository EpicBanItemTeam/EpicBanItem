package team.ebi.epicbanitem.util;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UpdateOperationRenderer {
  public static ImmutableList<Component> render(DataView view, UpdateOperation operation) {
    return ImmutableList.copyOf(operation.asComponent().children());
  }
}
