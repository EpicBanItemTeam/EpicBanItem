package team.ebi.epicbanitem.util;

import java.util.List;
import java.util.Optional;
import org.codehaus.plexus.util.StringUtils;
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
    if (query.parts().size() <= 1) return view.get(query);
    Optional<DataView> subView = view.getView(query.queryParts().get(0));
    Optional<List<?>> list = view.getList(query.queryParts().get(0));
    String index = query.parts().get(1);
    if (list.isPresent() && StringUtils.isNumeric(index)) {
      Object value = list.get().get(Integer.parseInt(index));
      if (value instanceof DataView) return get((DataView) value, query.popFirst().popFirst());
      return Optional.ofNullable(value);
    } else if (!subView.isPresent()) return Optional.empty();
    return get(subView.get(), query.popFirst());
  }
}
