package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class CommonUpdateOperation extends AbstractMap<DataQuery, UpdateOperation>
    implements UpdateOperation {

  private final Map<DataQuery, UpdateOperation> children;

  public CommonUpdateOperation(Map<DataQuery, UpdateOperation> children) {
    this.children = children;
  }

  @Override
  public UpdateOperation merge(UpdateOperation another) {
    if (Objects.isNull(another)) {
      return this;
    }
    Map<DataQuery, UpdateOperation> operations = Maps.newHashMap(children);
    another.forEach(
        (query, operation) ->
            operations.compute(
                query,
                (k, v) -> {
                  if (Objects.isNull(v)) {
                    return operation;
                  } else {
                    return operation.merge(v);
                  }
                }));

    return new CommonUpdateOperation(operations);
  }

  @Override
  public DataView process(DataView view) {
    for (UpdateOperation operation : children.values()) {
      operation.process(view);
    }
    return view;
  }

  @Override
  public UpdateOperation get(Object key) {
    return children.get(key);
  }

  @NotNull
  @Override
  public Set<Entry<DataQuery, UpdateOperation>> entrySet() {
    return children.entrySet();
  }

  @Override
  public @NotNull Component asComponent() {
    ImmutableList.Builder<ComponentLike> builder = ImmutableList.builder();
    for (UpdateOperation operation : values()) {
      builder.add(operation);
    }
    return Component.join(JoinConfiguration.newlines(), builder.build());
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
    CommonUpdateOperation that = (CommonUpdateOperation) o;
    return children.equals(that.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), children);
  }
}
