package team.ebi.epicbanitem.api.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

@FunctionalInterface
public interface QueryExpression {

  /**
   * @param query query path of the node
   * @param data predicate
   * @return The test result
   */
  Optional<TestResult> test(DataQuery query, DataView data);
}
