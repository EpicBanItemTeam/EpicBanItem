package team.ebi.epicbanitem.expression.query;

import java.text.MessageFormat;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataUtils;

public class ExistsQueryExpression implements QueryExpression {

  private static final String EXCEPTION =
      "$exists should be one of 0, 1, true or false. Current: {0}";
  private final boolean expect;

  public ExistsQueryExpression(boolean expect) {
    this.expect = expect;
  }

  public ExistsQueryExpression(DataView data, DataQuery query) {
    Object value =
        data.get(query)
            .orElseThrow(() -> new InvalidDataException(MessageFormat.format(EXCEPTION, "null")));
    if (value instanceof Integer) {
      int i = (int) value;
      expect = i == 1;
      if (i != 0 && i != 1) {
        throw new InvalidDataException(MessageFormat.format(EXCEPTION, i));
      }
    } else if (value instanceof Boolean) {
      expect = (boolean) value;
    } else {
      throw new InvalidDataException(MessageFormat.format(EXCEPTION, value));
    }
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(DataUtils.get(data, query).isPresent() == expect);
  }

  @Override
  public DataContainer toContainer() {
    return DataContainer.createNew().set(ROOT, expect);
  }
}
