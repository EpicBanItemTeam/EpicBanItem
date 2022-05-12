package team.ebi.epicbanitem.expression;

import java.util.Optional;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class RootQueryExpression implements QueryExpression, DataSerializable {
  private final QueryExpression expression;
  private final DataView view;

  public RootQueryExpression() {
    this(DataContainer.createNew());
  }

  public RootQueryExpression(DataView view) {
    this(view, DataQuery.of());
  }

  public RootQueryExpression(DataView view, DataQuery query) {
    this.view = view;
    this.expression = new CommonQueryExpression(view, query);
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
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return this.expression.query(query, data);
  }

  public static final class Builder extends AbstractDataBuilder<RootQueryExpression> {

    public Builder() {
      super(RootQueryExpression.class, 0);
    }

    @Override
    protected Optional<RootQueryExpression> buildContent(DataView container)
        throws InvalidDataException {
      return Optional.of(new RootQueryExpression(container));
    }
  }
}
