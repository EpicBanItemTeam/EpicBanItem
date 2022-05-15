package team.ebi.epicbanitem.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class RootUpdateExpression implements UpdateExpression, DataSerializable {
  private final UpdateExpression expression;

  public RootUpdateExpression(DataView view) {
    this.expression = new CommonUpdateExpression(view, DataQuery.of());
  }

  @Override
  public int contentVersion() {
    return 0;
  }

  @Override
  public DataContainer toContainer() {
    return expression.toContainer();
  }

  @Override
  public UpdateOperation update(QueryResult result, DataView data) {
    return this.expression.update(result, data);
  }

  public static final class Builder extends AbstractDataBuilder<RootUpdateExpression> {

    public Builder() {
      super(RootUpdateExpression.class, 0);
    }

    @Override
    protected Optional<RootUpdateExpression> buildContent(DataView container)
        throws InvalidDataException {
      return Optional.of(new RootUpdateExpression(container));
    }
  }
}
