package team.ebi.epicbanitem.api.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;

@FunctionalInterface
public interface QueryExpression {

  /**
   * @param query query path of the node
   * @param data predicate
   * @return The test result
   */
  Optional<QueryResult> query(DataQuery query, Object data);

  default Optional<QueryResult> query(Object data) {
    return query(DataQuery.of(), data);
  }
}
