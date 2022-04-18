package team.ebi.epicbanitem.expression;

import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class ReplaceUpdateOperation implements UpdateOperation {

  private final DataView value;

  public ReplaceUpdateOperation(DataView value) {
    this.value = value;
  }

  @Override
  public DataView process(DataView view) {
    return view.set(value.currentPath(), value);
  }
}
