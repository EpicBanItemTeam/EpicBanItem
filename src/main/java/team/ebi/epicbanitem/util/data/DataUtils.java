package team.ebi.epicbanitem.util.data;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.codehaus.plexus.util.StringUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;

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
    Optional<List<?>> list = view.get(firstQuery).flatMap(DataUtils::asList);
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

  public static DataView dataToExpression(DataView view) {
    DataContainer container = DataContainer.createNew();
    view.values(true).forEach((key, value) -> {
      if (!(value instanceof DataView)) {
        asList(value).ifPresentOrElse(
            list ->
                container.createView(DataQuery.of(key.toString())).set(ExpressionQueries.ALL, list),
            () -> container.set(DataQuery.of(key.toString()), value));
      }
    });
    return container;
  }

  public static Optional<List<?>> asList(@Nullable Object obj) {
    if (obj == null) {
      return Optional.empty();
    }

    if (obj instanceof List) {
      return Optional.of((List<?>) obj);
    }

    final Class<?> clazz = obj.getClass();
    if (clazz.isArray()) {
      if (clazz.getComponentType().isPrimitive()) {
        return Optional.of(primitiveArrayToList(obj));
      }

      return Optional.of(Arrays.asList((Object[]) obj));
    }

    return Optional.empty();
  }

  private static List<?> primitiveArrayToList(Object obj) {
    if (obj instanceof boolean[] array) {
      return Booleans.asList(array);
    } else if (obj instanceof char[] array) {
      return Chars.asList(array);
    } else if (obj instanceof byte[] array) {
      return Bytes.asList(array);
    } else if (obj instanceof short[] array) {
      return Shorts.asList(array);
    } else if (obj instanceof int[] array) {
      return Ints.asList(array);
    } else if (obj instanceof long[] array) {
      return Longs.asList(array);
    } else if (obj instanceof float[] array) {
      return Floats.asList(array);
    } else if (obj instanceof double[] array) {
      return Doubles.asList(array);
    }

    return Collections.emptyList();
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
