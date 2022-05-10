package team.ebi.epicbanitem.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

public class QueryResultRenderer {
  private static final Component COLON = Component.text(": ");
  private static final Component LEFT_SQUARE_BRACKET = Component.text("[");
  private static final Component RIGHT_SQUARE_BRACKET = Component.text("]");
  private static final Component LEFT_CURLY_BRACKET = Component.text("{");
  private static final Component RIGHT_CURLY_BRACKET = Component.text("}");
  private static final Component INDENT = Component.text(Strings.repeat(" ", 2));

  private static Component renderKey(String key, DataQuery path) {
    return Component.text()
        .content(key)
        .append(COLON)
        .hoverEvent(Component.text(path.toString()))
        .clickEvent(ClickEvent.copyToClipboard(path.toString()))
        .build();
  }

  private static ImmutableList<Component> wrapList(
      @Nullable Component key, ImmutableList<Component> input) {
    return wrap(key, input, LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET);
  }

  private static ImmutableList<Component> wrapObject(
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
    components.addAll(input.stream().map(INDENT::append).collect(ImmutableList.toImmutableList()));
    return components.add(rightBracket).build();
  }

  private static Component wrapValue(Component key, Component value) {
    return key.append(value);
  }

  private static Style style(Object value) {
    Style.Builder builder = Style.style();
    String toCopy;
    if (value instanceof Boolean) {
      builder.color(NamedTextColor.LIGHT_PURPLE);
      toCopy = "\"" + value + "\"";
    } else if (value instanceof String) {
      builder.color(NamedTextColor.GREEN);
      toCopy = (String) value;
    } else {
      builder.color(NamedTextColor.AQUA);
      toCopy = value.toString();
    }
    builder.hoverEvent(Component.text(toCopy));
    builder.clickEvent(ClickEvent.copyToClipboard(toCopy));
    return builder.build();
  }

  private static ImmutableList<Component> renderList(
      List<?> list, DataQuery expandedQuery, @Nullable QueryResult result) {
    ImmutableList.Builder<Component> components = ImmutableList.builder();
    ImmutableMap<String, QueryResult> children =
        result == null ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    for (int i = 0; i < list.size(); i++) {
      String key = Integer.toString(i);
      Object value = list.get(i);
      Style.Builder style = Style.style();
      if (children.get(key) != null) style.decorate(TextDecoration.BOLD);
      if (value instanceof DataView) {
        components.addAll(
            wrapObject(
                renderKey(key, expandedQuery).style(builder -> builder.merge(style.build())),
                renderView((DataView) value, expandedQuery, children.get(key))));
      } else {
        components.add(
            wrapValue(
                renderKey(key, expandedQuery).style(builder -> builder.merge(style.build())),
                Component.text(value.toString()).style(style.merge(style(value)))));
      }
    }
    return components.build();
  }

  private static ImmutableList<Component> renderView(
      DataView view, DataQuery expandedQuery, @Nullable QueryResult result) {
    ImmutableList.Builder<Component> components = ImmutableList.builder();
    ImmutableMap<String, QueryResult> children =
        result == null ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    for (DataQuery query : view.keys(false)) {
      Optional<DataView> subView = view.getView(query);
      Optional<List<?>> list = view.getList(query);
      Optional<Object> value = DataViewUtils.get(view, query);
      String key = query.parts().get(0);
      DataQuery currentExpandedQuery = expandedQuery.then(key);
      Style.Builder style = Style.style();
      if (children.get(key) != null) style.decorate(TextDecoration.BOLD);
      if (subView.isPresent())
        components.addAll(
            wrapObject(
                renderKey(key, currentExpandedQuery).style(builder -> builder.merge(style.build())),
                renderView(subView.get(), currentExpandedQuery, children.get(key))));
      else if (list.isPresent())
        components.addAll(
            wrapList(
                renderKey(key, currentExpandedQuery).style(builder -> builder.merge(style.build())),
                renderList(list.get(), currentExpandedQuery, children.get(key))));
      else
        value.ifPresent(
            o ->
                components.add(
                    wrapValue(
                        renderKey(key, currentExpandedQuery)
                            .style(builder -> builder.merge(style.build())),
                        Component.text(o.toString()).style(style.merge(style(o))))));
    }
    return components.build();
  }

  public static ImmutableList<Component> render(DataView view, @Nullable QueryResult result) {
    return wrapObject(null, renderView(view, DataQuery.of(), result));
  }
}
