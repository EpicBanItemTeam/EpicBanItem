package team.ebi.epicbanitem.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;

public class RootQueryExpression implements QueryExpression, DataSerializable {
  private final QueryExpression expression;
  private final DataView view;

  public RootQueryExpression(DataView view) {
    this.view = view;
    this.expression = new CommonQueryExpression(view);
  }

  @Override
  public int contentVersion() {
    return 0;
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew()
        .set(Queries.CONTENT_VERSION, contentVersion())
        .set(DataQuery.of("expression"), view);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    return this.expression.test(query, data);
  }
}
