package team.ebi.epicbanitem.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class DataViewComponentRenderer {
  private static final Component COLON = Component.text(":");
  private static final Component LEFT_SQUARE_BRACKET = Component.text("[");
  private static final Component RIGHT_SQUARE_BRACKET = Component.text("]");
  private static final Component LEFT_CURLY_BRACKET = Component.text("{");
  private static final Component RIGHT_CURLY_BRACKET = Component.text("}");
  private static final Component INDENT = Component.text(Strings.repeat(" ", 2));

  public static ImmutableList<Component> render(DataView view, QueryResult result) {
    return render(view, result, Style.style());
  }

  public static ImmutableList<Component> render(
      DataView view, @Nullable QueryResult result, Style.Builder callbackStyle) {
    ImmutableList.Builder<Component> components = ImmutableList.builder();
    ImmutableMap<String, QueryResult> children =
        result == null ? ImmutableMap.of() : result.children();

    Optional<List<?>> list = view.getList(DataQuery.of());
    boolean isList = list.isPresent();
    Set<DataQuery> keys = view.keys(false);
    boolean isLeaf = keys.isEmpty(); // Can be list or leaf node
    DataQuery currentPath = view.currentPath();
    DataQuery lastPath = currentPath.last();
    String path = currentPath.toString();

    if (isList) {
      List<?> values = list.get();
      components.add(
          Component.text()
              .content(lastPath.toString())
              .append(COLON)
              .append(Component.space())
              .append(LEFT_SQUARE_BRACKET)
              .build());
      for (int i = 0; i < values.size(); i++) {
        Object value = values.get(i);
        String key = Integer.toString(i);
        QueryResult subResult = children.get(key);
        if (subResult != null) callbackStyle.decorate(TextDecoration.BOLD);
        components.add(
            INDENT
                .append(
                    Component.text(value.toString())
                        .hoverEvent(Component.text(path))
                        .clickEvent(ClickEvent.copyToClipboard(path)))
                .style(style(Style.style().merge(callbackStyle.build()), value).build()));
      }
      components.add(RIGHT_SQUARE_BRACKET);
    } else if (isLeaf) {
      Object value = view.get(DataQuery.of()).orElse(null);
      if (result != null) callbackStyle.decorate(TextDecoration.BOLD);
      TextComponent.Builder builder =
          Component.text()
              .content(lastPath.toString())
              .append(COLON)
              .hoverEvent(Component.text(path))
              .clickEvent(ClickEvent.copyToClipboard(path))
              .style(style(Style.style().merge(callbackStyle.build()), value).build());
      if (value != null) builder.append(Component.space()).append(Component.text(value.toString()));
      components.add(builder.build());
    } else {
      components.add(
          Component.text()
              .content(lastPath.toString())
              .hoverEvent(Component.text(path))
              .clickEvent(ClickEvent.copyToClipboard(path))
              .append(COLON)
              .append(Component.space())
              .append(LEFT_CURLY_BRACKET)
              .build());
      for (DataQuery query : keys) {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        DataView subView = view.getView(query).get();
        components.addAll(
            render(subView, children.get(query.last().toString()), callbackStyle).stream()
                .map(INDENT::append)
                .collect(Collectors.toList()));
      }
      components.add(RIGHT_CURLY_BRACKET);
    }
    return components.build();
  }

  private static Style.Builder style(Style.Builder builder, Object value) {
    if (value instanceof Boolean) {
      builder.color(NamedTextColor.LIGHT_PURPLE);
    } else if (value instanceof String) {
      builder.color(NamedTextColor.GREEN);
    } else {
      builder.color(NamedTextColor.AQUA);
    }
    return builder;
  }
}
