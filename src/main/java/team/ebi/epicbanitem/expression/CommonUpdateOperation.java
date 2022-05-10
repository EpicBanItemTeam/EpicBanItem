package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class CommonUpdateOperation extends AbstractMap<DataQuery, UpdateOperation>
    implements UpdateOperation {
  private final ImmutableMap<DataQuery, UpdateOperation> children;

  public CommonUpdateOperation(Map<DataQuery, UpdateOperation> children) {
    this.children = ImmutableMap.copyOf(children);
  }

  @Override
  public UpdateOperation merge(UpdateOperation another) {
    if (Objects.isNull(another)) return this;
    Map<DataQuery, UpdateOperation> operations = Maps.newHashMap(children);
    another.forEach(
        (query, operation) ->
            operations.compute(
                query,
                (k, v) -> {
                  if (Objects.isNull(v)) return operation;
                  // else if (!(operation instanceof ReplaceUpdateOperation
                  //     || v instanceof ReplaceUpdateOperation)) return operation.merge(v);
                  else return operation.merge(v);
                }));

    return new CommonUpdateOperation(operations);
  }

  @Override
  public DataView process(DataView view) {
    for (UpdateOperation operation : children.values()) operation.process(view);
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
    TextComponent.Builder builder = Component.text();
    for (UpdateOperation operation : values()) builder.append(operation);
    return builder.build();
  }
}
