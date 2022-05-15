package team.ebi.epicbanitem.expression;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Objects;
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
    return Component.translatable("epicbanitem.operations.replace")
        .args(Component.text(query.toString()), Component.text(value.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ReplaceUpdateOperation that = (ReplaceUpdateOperation) o;
    return value.equals(that.value) && query.equals(that.query);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value, query);
  }
}
