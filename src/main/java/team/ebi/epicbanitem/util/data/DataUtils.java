package team.ebi.epicbanitem.util.data;

import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.codehaus.plexus.util.StringUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Coerce;

public final class DataUtils {

  private DataUtils() {
  }

  /**
   * Support to get value from array
   *
   * @param view  view to get
   * @param query current query
   * @return value
   */
  public static Optional<Object> get(DataView view, DataQuery query) {
    if (query.parts().size() <= 1) {
      return view.get(query);
    }
    DataQuery firstQuery = query.queryParts().get(0);
    Optional<DataView> subView = view.getView(firstQuery);
    // Coerce#asList(Object) will process primitive types
    Optional<List<?>> list = view.get(firstQuery).flatMap(Coerce::asList);
    String index = query.parts().get(1);
    if (StringUtils.isNumeric(index) && list.isPresent()) {
      Object value = list.get().get(Integer.parseInt(index));
      if (value instanceof DataView viewValue) {
        // The first is index. Second is the key
        return get(viewValue, query.popFirst().popFirst());
      }
      return Optional.of(value);
    }
    return subView.flatMap(it -> get(it, query.popFirst()));
  }

  public static Component objectName(SerializableDataHolder holder) {
    Component objectName =
        holder
            .get(Keys.DISPLAY_NAME)
            .orElseGet(
                () -> {
                  if (holder instanceof BlockSnapshot snapshot) {
                    return snapshot.state().type().asComponent();
                  } else {
                    return ItemTypes.AIR.get().asComponent();
                  }
                });
    if (holder instanceof ItemStackSnapshot snapshot) {
      objectName = objectName.hoverEvent(snapshot);
    }
    return objectName;
  }
}
