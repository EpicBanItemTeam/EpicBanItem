package team.ebi.epicbanitem.util;

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
    Optional<DataView> subView = view.getView(query.queryParts().get(0));
    Optional<List<?>> list = view.getList(query.queryParts().get(0));
    String index = query.parts().get(1);
    if (list.isPresent() && StringUtils.isNumeric(index)) {
      Object value = list.get().get(Integer.parseInt(index));
      if (value instanceof DataView viewValue) {
        return get(viewValue, query.popFirst().popFirst());
      }
      return Optional.ofNullable(value);
    } else if (subView.isEmpty()) {
      return Optional.empty();
    }
    return get(subView.get(), query.popFirst());
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
