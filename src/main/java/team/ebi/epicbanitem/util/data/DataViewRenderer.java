package team.ebi.epicbanitem.util.data;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

public final class DataViewRenderer {

  static final Component COLON = Component.text(": ");
  static final Component LEFT_SQUARE_BRACKET = Component.text("[");
  static final Component RIGHT_SQUARE_BRACKET = Component.text("]");
  static final Component LEFT_CURLY_BRACKET = Component.text("{");
  static final Component RIGHT_CURLY_BRACKET = Component.text("}");
  static final Component INDENT = Component.text(Strings.repeat(" ", 2));

  private DataViewRenderer() {
  }

  static List<Component> wrapList(
      @Nullable Component key, List<Component> input) {
    return wrap(key, input, LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET);
  }

  static List<Component> wrapObject(
      @Nullable Component key, List<Component> input) {
    return wrap(key, input, LEFT_CURLY_BRACKET, RIGHT_CURLY_BRACKET);
  }

  private static List<Component> wrap(
      @Nullable Component key,
      List<Component> input,
      Component leftBracket,
      Component rightBracket) {
    var components = Lists.<Component>newArrayList();
    var keyComponent = Component.text();
    if (Objects.nonNull(key)) {
      keyComponent.append(key);
    }
    if (input.isEmpty()) {
      components.add(keyComponent.append(leftBracket).append(rightBracket).build());
    } else {
      components.add(keyComponent.append(leftBracket).build());
      components.addAll(input.stream().map(INDENT::append).toList());
      components.add(rightBracket);
    }
    return components;
  }

  static Style valueStyle(Object value) {
    var builder = Style.style();
    var toCopy = valueString(value);
    if (value instanceof Boolean) {
      builder.color(NamedTextColor.LIGHT_PURPLE);
    } else if (value instanceof String) {
      builder.color(NamedTextColor.GREEN);
    } else {
      builder.color(NamedTextColor.AQUA);
    }
    builder.hoverEvent(Component.text(toCopy));
    builder.clickEvent(ClickEvent.copyToClipboard(toCopy));
    return builder.build();
  }

  static String valueString(Object value) {
    if (value instanceof String) {
      return "\"" + value + "\"";
    } else {
      return value.toString();
    }
  }

  private static List<Component> renderList(List<?> list) {
    var components = Lists.<Component>newArrayList();
    for (int i = 0; i < list.size(); i++) {
      var key = Integer.toString(i);
      var value = list.get(i);
      if (value instanceof DataView subView) {
        components.addAll(
            wrapObject(Component.text(key).append(COLON), renderView(subView)));
      } else {
        components.add(
            Component.text(key)
                .append(COLON)
                .append(Component.text(value.toString()).style(valueStyle(value))));
      }
    }
    return components;
  }

  private static List<Component> renderView(DataView view) {
    var components = Lists.<Component>newArrayList();
    for (DataQuery query : view.keys(false)) {
      var value = view.get(query).orElseThrow();
      var key = query.parts().get(0);
      if (value instanceof DataView subView) {
        components.addAll(wrapObject(Component.text(key).append(COLON), renderView(subView)));
      } else if (value instanceof List<?> list) {
        components.addAll(wrapList(Component.text(key).append(COLON), renderList(list)));
      } else {
        components.add(
            Component.text(key)
                .append(COLON)
                .append(Component.text(value.toString()).style(valueStyle(value))));
      }
    }
    return components;
  }

  public static List<Component> render(DataView view) {
    return wrapObject(null, renderView(view));
  }
}
