package team.ebi.epicbanitem.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

  static ImmutableList<Component> wrapList(
      @Nullable Component key, ImmutableList<Component> input) {
    return wrap(key, input, LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET);
  }

  static ImmutableList<Component> wrapObject(
      @Nullable Component key, ImmutableList<Component> input) {
    return wrap(key, input, LEFT_CURLY_BRACKET, RIGHT_CURLY_BRACKET);
  }

  private static ImmutableList<Component> wrap(
      @Nullable Component key,
      ImmutableList<Component> input,
      Component leftBracket,
      Component rightBracket) {
    ImmutableList.Builder<Component> components = ImmutableList.builder();
    TextComponent.Builder keyComponent = Component.text();
    if (Objects.nonNull(key)) keyComponent.append(key);
    components.add(keyComponent.append(leftBracket).build());
    //noinspection UnstableApiUsage
    components.addAll(input.stream().map(INDENT::append).collect(ImmutableList.toImmutableList()));
    return components.add(rightBracket).build();
  }

  static Style valueStyle(Object value) {
    Style.Builder builder = Style.style();
    String toCopy = valueString(value);
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

  private static ImmutableList<Component> renderList(List<?> list) {
    ImmutableList.Builder<Component> components = ImmutableList.builder();
    for (int i = 0; i < list.size(); i++) {
      String key = Integer.toString(i);
      Object value = list.get(i);
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
    return components.build();
  }

  private static ImmutableList<Component> renderView(DataView view) {
    ImmutableList.Builder<Component> components = ImmutableList.builder();
    for (DataQuery query : view.keys(false)) {
      Object value = view.get(query).orElseThrow();
      String key = query.parts().get(0);
      if (value instanceof DataView subView)
        components.addAll(wrapObject(Component.text(key).append(COLON), renderView(subView)));
       else if(value instanceof List<?> list)
        components.addAll(wrapList(Component.text(key).append(COLON), renderList(list)));
      else
        components.add(
            Component.text(key)
                .append(COLON)
                .append(Component.text(value.toString()).style(valueStyle(value))));
    }
    return components.build();
  }

  public static ImmutableList<Component> render(DataView view) {
    return wrapObject(null, renderView(view));
  }
}
