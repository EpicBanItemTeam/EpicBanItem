package team.ebi.epicbanitem.api.expression;

import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.api.ItemQueries;
import team.ebi.epicbanitem.expression.ExpressionServiceImpl;

@ImplementedBy(ExpressionServiceImpl.class)
public interface ExpressionService {

  Set<DataQuery> IGNORED =
      Set.of(
          Queries.CONTENT_VERSION,
          Queries.WORLD_KEY,
          ItemQueries.UNSAFE_DAMAGE,
          ItemQueries.CREATOR,
          ItemQueries.X,
          ItemQueries.Y,
          ItemQueries.Z,
          ItemQueries.BLOCK_ID);

  static DataView cleanup(DataView view) {
    DataContainer container = DataContainer.createNew();
    view.keys(true)
        .forEach(
            key -> {
              Object value = view.get(key).orElseThrow();
              if (value instanceof DataView) return;
              if (IGNORED.contains(key)) {
                return;
              }
              if (IGNORED.stream().anyMatch(query -> key.toString().contains(query.toString()))) {
                return;
              }
              container.set(key, value);
            });
    return container;
  }

  List<Component> renderQueryResult(DataView view, QueryResult result);
}
