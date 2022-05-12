package team.ebi.epicbanitem.expression;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.ExpressionService;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.util.QueryResultRenderer;

public class ExpressionServiceImpl implements ExpressionService {

  @Override
  public List<Component> renderQueryResult(DataView view, QueryResult result) {
    return QueryResultRenderer.render(view, result);
  }
}
