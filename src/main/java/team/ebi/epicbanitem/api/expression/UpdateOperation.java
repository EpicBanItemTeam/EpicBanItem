package team.ebi.epicbanitem.api.expression;

import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.expression.CommonUpdateOperation;
import team.ebi.epicbanitem.expression.RemoveUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;

public interface UpdateOperation extends Map<DataQuery, UpdateOperation>, ComponentLike {

  static UpdateOperation common() {
    return common(Map.of());
  }

  static UpdateOperation common(Map<DataQuery, UpdateOperation> children) {
    return new CommonUpdateOperation(children);
  }

  static UpdateOperation remove(DataQuery query) {
    return new RemoveUpdateOperation(query);
  }

  static UpdateOperation replace(DataQuery query, Object value) {
    return new ReplaceUpdateOperation(query, value);
  }

  /**
   * @param view {@link DataView} to update
   * @return The same {@link DataView} object that is updated
   */
  DataView process(DataView view);

  default UpdateOperation merge(@Nullable UpdateOperation another) {
    return Objects.isNull(another) ? this : another;
  }
}
