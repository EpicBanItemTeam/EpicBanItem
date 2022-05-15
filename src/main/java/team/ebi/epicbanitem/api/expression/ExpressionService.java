package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableSet;
import com.google.inject.ImplementedBy;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.api.ItemQueries;
import team.ebi.epicbanitem.expression.ExpressionServiceImpl;

@ImplementedBy(ExpressionServiceImpl.class)
public interface ExpressionService {
  ImmutableSet<DataQuery> IGNORED =
      ImmutableSet.of(
          Queries.CONTENT_VERSION,
          Queries.WORLD_KEY,
          ItemQueries.UNSAFE_DAMAGE,
          ItemQueries.CREATOR);

  static DataView cleanup(DataView view) {
    DataContainer container = DataContainer.createNew();
    view.values(true)
        .forEach(
            (key, o) -> {
              if (!IGNORED.contains(key)) container.set(key, o);
            });
    return container;
  }

  List<Component> renderQueryResult(DataView view, QueryResult result);
}
