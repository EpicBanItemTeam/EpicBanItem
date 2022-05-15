package team.ebi.epicbanitem.api.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;

public interface QueryExpression extends DataSerializable {
  DataQuery ROOT = DataQuery.of("ExpressionRoot");

  /**
   * @param query query path of the node
   * @param data predicate
   * @return The test result
   */
  Optional<QueryResult> query(DataQuery query, DataView data);

  default Optional<QueryResult> query(DataView data) {
    return query(DataQuery.of(), data);
  }

  @Override
  default int contentVersion() {
    return 0;
  }
}
