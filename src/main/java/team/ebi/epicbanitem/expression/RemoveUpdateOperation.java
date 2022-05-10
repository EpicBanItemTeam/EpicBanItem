package team.ebi.epicbanitem.expression;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class RemoveUpdateOperation extends AbstractMap<DataQuery, UpdateOperation>
    implements UpdateOperation {

  private final DataQuery query;

  public RemoveUpdateOperation(DataQuery query) {
    this.query = query;
  }

  @Override
  public DataView process(DataView view) {
    return view.remove(query);
  }

  @NotNull
  @Override
  public Set<Entry<DataQuery, UpdateOperation>> entrySet() {
    return Collections.emptySet();
  }

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("epicbanitem.operations.remove");
  }
}
