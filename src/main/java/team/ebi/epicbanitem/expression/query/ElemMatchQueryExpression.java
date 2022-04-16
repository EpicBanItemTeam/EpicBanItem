package team.ebi.epicbanitem.expression.query;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.TestResult;
import team.ebi.epicbanitem.expression.CommonQueryExpression;

public class ElemMatchQueryExpression implements QueryExpression {
  private final QueryExpression expression;

  public ElemMatchQueryExpression(QueryExpression expression) {
    this.expression = expression;
  }

  public ElemMatchQueryExpression(DataView view) {
    this.expression = new CommonQueryExpression(view);
  }

  @Override
  public Optional<TestResult> test(DataQuery query, DataView data) {
    Optional<List<DataView>> viewsOpt = data.getViewList(query);
    if (!viewsOpt.isPresent() || viewsOpt.get().isEmpty()) return TestResult.failed();
    List<DataView> views = viewsOpt.get();
    ImmutableMap.Builder<String, TestResult> builder = ImmutableMap.builder();
    boolean matched = false;
    for (int i = 0; i < views.size(); i++) {
      DataView view = views.get(i);
      String key = Integer.toString(i);
      Optional<TestResult> result = this.expression.test(DataQuery.of(), view);
      if (result.isPresent()) {
        builder.put(key, result.get());
        matched = true;
      }
    }
    return TestResult.fromArray(matched, builder.build());
  }
}
