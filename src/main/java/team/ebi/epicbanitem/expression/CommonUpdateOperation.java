package team.ebi.epicbanitem.expression;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class CommonUpdateOperation implements UpdateOperation {
  private final ImmutableMap<DataQuery, UpdateOperation> children;

  public CommonUpdateOperation(Map<DataQuery, UpdateOperation> children) {
    this.children = ImmutableMap.copyOf(children);
  }

  @Override
  public ImmutableMap<DataQuery, UpdateOperation> children() {
    return children;
  }

  @Override
  public UpdateOperation merge(UpdateOperation another) {
    if (Objects.isNull(another)) return this;
    Map<DataQuery, UpdateOperation> operations = Maps.newHashMap(children);
    another
        .children()
        .forEach(
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
}
