package team.ebi.epicbanitem.expression;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class RemoveUpdateOperation implements UpdateOperation {

  private final DataQuery query;

  public RemoveUpdateOperation(DataQuery query) {
    this.query = query;
  }

  @Override
  public DataView process(DataView view) {
    return view.remove(query);
  }
}
