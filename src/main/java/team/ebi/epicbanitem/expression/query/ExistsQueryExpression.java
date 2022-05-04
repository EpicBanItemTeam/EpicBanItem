package team.ebi.epicbanitem.expression.query;

import java.text.MessageFormat;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.DataPreconditions;
import team.ebi.epicbanitem.util.DataViewUtils;

public class ExistsQueryExpression implements QueryExpression {
  private static final String exception =
      "$exists should be one of 0, 1, true or false. Current: {}";
  private final boolean expect;

  public ExistsQueryExpression(DataView data, DataQuery query) {
    Object value =
        data.get(query)
            .orElseThrow(() -> new InvalidDataException(MessageFormat.format(exception, "null")));
    if (value instanceof Integer) {
      int i = (int) value;
      expect = i == 1;
      DataPreconditions.checkData(i == 0 || i == 1, exception, i);
    } else if (value instanceof Boolean) {
      expect = (boolean) value;
    } else throw new InvalidDataException(MessageFormat.format(exception, value));
  }

  @Override
  public Optional<QueryResult> query(DataQuery query, DataView data) {
    return QueryResult.from(DataViewUtils.get(data, query).isPresent() == expect);
  }
}
