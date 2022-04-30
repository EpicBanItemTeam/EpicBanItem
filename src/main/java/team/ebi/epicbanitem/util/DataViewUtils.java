package team.ebi.epicbanitem.util;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

public final class DataViewUtils {
  /**
   * Support to get value from array
   *
   * @param view view to get
   * @param query current query
   * @return value
   */
  public static Optional<Object> get(DataView view, DataQuery query) {
    Optional<DataView> subView = view.getView(DataQuery.of());
    Optional<List<?>> list = view.getList(DataQuery.of());
    if (list.isPresent())
      return Optional.ofNullable(list.get().get(Integer.parseInt(query.parts().get(0))));
    else if (!subView.isPresent()) {
      if (query.parts().isEmpty()) return Optional.of(view.get(DataQuery.of()));
      else return Optional.empty();
    }
    return get(subView.get(), query.popFirst());
  }
}
