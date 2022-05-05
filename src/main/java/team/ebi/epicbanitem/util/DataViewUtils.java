package team.ebi.epicbanitem.util;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.Contract;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;

public final class DataViewUtils {
  private static final ImmutableSet<DataQuery> IGNORED =
      ImmutableSet.of(
          Queries.CONTENT_VERSION,
          Queries.WORLD_KEY,
          DataQuery.of("Position"),
          DataQuery.of("BlockState"));

  /**
   * Support to get value from array
   *
   * @param view view to get
   * @param query current query
   * @return value
   */
  public static Optional<Object> get(DataView view, DataQuery query) {
    if (query.parts().size() <= 1) return view.get(query);
    Optional<DataView> subView = view.getView(query.queryParts().get(0));
    Optional<List<?>> list = view.getList(query.queryParts().get(0));
    String index = query.parts().get(1);
    if (list.isPresent() && StringUtils.isNumeric(index)) {
      Object value = list.get().get(Integer.parseInt(index));
      if (value instanceof DataView) return get((DataView) value, query.popFirst().popFirst());
      return Optional.ofNullable(value);
    } else if (subView.isEmpty()) return Optional.empty();
    return get(subView.get(), query.popFirst());
  }

  @Contract(pure = true)
  public static DataView cleanup(DataView view) {
    DataContainer container = DataContainer.createNew();
    for (DataQuery key : view.keys(false))
      if (!IGNORED.contains(key)) container.set(key, view.get(key).get());
    return container;
  }
}
