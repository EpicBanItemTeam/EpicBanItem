package team.ebi.epicbanitem.util;

import static team.ebi.epicbanitem.util.DataViewRenderer.COLON;
import static team.ebi.epicbanitem.util.DataViewRenderer.valueString;
import static team.ebi.epicbanitem.util.DataViewRenderer.valueStyle;
import static team.ebi.epicbanitem.util.DataViewRenderer.wrapList;
import static team.ebi.epicbanitem.util.DataViewRenderer.wrapObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;

public class QueryResultRenderer {
  private static Component renderKey(String key, DataQuery path) {
    return Component.text()
        .content(key)
        .hoverEvent(Component.text(path.toString()))
        .clickEvent(ClickEvent.copyToClipboard(path.toString()))
        .build();
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
                renderKey(key, expandedQuery).applyFallbackStyle(style.build()).append(COLON),
                renderView((DataView) value, expandedQuery, children.get(key))));
      } else {
        String pair = expandedQuery.then(key) + ": " + valueString(value);
        components.add(
            renderKey(key, expandedQuery)
                .applyFallbackStyle(style.build())
                .append(
                    COLON
                        .hoverEvent(Component.text(pair))
                        .clickEvent(ClickEvent.copyToClipboard(pair)))
                .append(Component.text(value.toString()).style(style.merge(valueStyle(value)))));
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
      Object value = view.get(query).orElseThrow();
      String key = query.parts().get(0);
      DataQuery currentExpandedQuery = expandedQuery.then(key);
      Style.Builder style = Style.style();
      if (children.get(key) != null) style.decorate(TextDecoration.BOLD);
      if (value instanceof DataView subView)
        components.addAll(
            wrapObject(
                renderKey(key, currentExpandedQuery)
                    .applyFallbackStyle(style.build())
                    .append(COLON),
                renderView(subView, currentExpandedQuery, children.get(key))));
      else if(value instanceof List<?> list)
        components.addAll(
            wrapList(
                renderKey(key, currentExpandedQuery)
                    .applyFallbackStyle(style.build())
                    .append(COLON),
                renderList(list, currentExpandedQuery, children.get(key))));
      else {
        String pair = expandedQuery.then(key) + ": " + valueString(value);
        components.add(
            renderKey(key, currentExpandedQuery)
                .applyFallbackStyle(style.build())
                .append(
                    COLON
                        .hoverEvent(Component.text(pair))
                        .clickEvent(ClickEvent.copyToClipboard(pair)))
                .append(Component.text(value.toString()).style(style.merge(valueStyle(value)))));
      }
    }
    return components.build();
  }

  public static ImmutableList<Component> render(DataView view, @Nullable QueryResult result) {
    return wrapObject(null, renderView(view, DataQuery.of(), result));
  }
}
