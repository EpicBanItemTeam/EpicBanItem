package team.ebi.epicbanitem.expression;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class ReplaceUpdateOperation extends AbstractMap<DataQuery, UpdateOperation>
    implements UpdateOperation {

  private final Object value;
  private final DataQuery query;

  public ReplaceUpdateOperation(DataQuery query, Object value) {
    this.query = query;
    this.value = value;
  }

  @Override
  public DataView process(DataView view) {
    return view.set(query, value);
  }

  @NotNull
  @Override
  public Set<Entry<DataQuery, UpdateOperation>> entrySet() {
    return Collections.emptySet();
  }

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("epicbanitem.operations.replace");
  }
}
