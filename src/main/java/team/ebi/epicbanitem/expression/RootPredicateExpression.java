package team.ebi.epicbanitem.expression;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.PredicateExpression;
import team.ebi.epicbanitem.api.expression.TestResult;

public class RootPredicateExpression implements PredicateExpression, DataSerializable {
  private final Set<PredicateExpression> expressions = Sets.newHashSet();

  public RootPredicateExpression(DataView view) {

  }

  @Override
  public int contentVersion() {
    return 0;
  }

  @Override
  public DataContainer toContainer() {
    return null;
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return Optional.empty();
  }
}
