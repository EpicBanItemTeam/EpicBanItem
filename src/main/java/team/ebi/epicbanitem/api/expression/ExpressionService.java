package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableSet;
import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.expression.ExpressionServiceImpl;

@ImplementedBy(ExpressionServiceImpl.class)
public interface ExpressionService {
  ImmutableSet<DataQuery> IGNORED =
      ImmutableSet.of(
          Queries.CONTENT_VERSION,
          Queries.WORLD_KEY,
          DataQuery.of("UnsafeDamage"),
          DataQuery.of("Position"),
          DataQuery.of("BlockState"));

  default Optional<QueryResult> query(QueryExpression expression, DataView view) {
    return expression.query(view);
  }

  default UpdateOperation update(UpdateExpression expression, QueryResult result, DataView view) {
    return expression.update(result, view);
  }

  List<Component> renderQuery(DataView view, QueryResult result);

  static DataView cleanup(DataView view) {
    DataContainer container = DataContainer.createNew();
    view.values(false)
        .forEach(
            (key, o) -> {
              if (!IGNORED.contains(key)) container.set(key, o);
            });
    return container;
  }
}
