package team.ebi.epicbanitem.util.data;

import static team.ebi.epicbanitem.util.data.DataViewRenderer.COLON;
import static team.ebi.epicbanitem.util.data.DataViewRenderer.valueString;
import static team.ebi.epicbanitem.util.data.DataViewRenderer.valueStyle;
import static team.ebi.epicbanitem.util.data.DataViewRenderer.wrapList;
import static team.ebi.epicbanitem.util.data.DataViewRenderer.wrapObject;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import team.ebi.epicbanitem.api.expression.QueryResult;

public final class QueryResultRenderer {

  private QueryResultRenderer() {
  }

  private static Component renderKey(String key, DataQuery path) {
    return Component.text()
        .content(key)
        .hoverEvent(Component.text(path.toString()))
        .clickEvent(ClickEvent.copyToClipboard(path.toString()))
        .build();
  }

  private static List<Component> renderList(
      List<?> list, DataQuery expandedQuery, @Nullable QueryResult result) {
    var components = Lists.<Component>newArrayList();
    var children =
        result == null ? Map.<String, QueryResult>of() : Map.copyOf(result);
    for (int i = 0; i < list.size(); i++) {
      var key = Integer.toString(i);
      var value = list.get(i);
      var style = Style.style();
      if (children.get(key) != null) {
        style.decorate(TextDecoration.BOLD);
      }
      if (value instanceof DataView view) {
        components.addAll(
            wrapObject(
                renderKey(key, expandedQuery).applyFallbackStyle(style.build()).append(COLON),
                renderView(view, expandedQuery, children.get(key))));
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
    return List.copyOf(components);
  }

  private static List<Component> renderView(
      DataView view, DataQuery expandedQuery, @Nullable QueryResult result) {
    var components = Lists.<Component>newArrayList();
    var children =
        result == null ? Map.<String, QueryResult>of() : Map.copyOf(result);
    for (DataQuery query : view.keys(false)) {
      var value = view.get(query).orElseThrow();
      var key = query.parts().get(0);
      var currentExpandedQuery = expandedQuery.then(key);
      var style = Style.style();
      if (children.get(key) != null) {
        style.decorate(TextDecoration.BOLD);
      }
      if (value instanceof DataView subView) {
        components.addAll(
            wrapObject(
                renderKey(key, currentExpandedQuery)
                    .applyFallbackStyle(style.build())
                    .append(COLON),
                renderView(subView, currentExpandedQuery, children.get(key))));
      } else if (value instanceof List<?> list) {
        components.addAll(
            wrapList(
                renderKey(key, currentExpandedQuery)
                    .applyFallbackStyle(style.build())
                    .append(COLON),
                renderList(list, currentExpandedQuery, children.get(key))));
      } else {
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
    return components;
  }

  public static List<Component> render(DataView view, @Nullable QueryResult result) {
    return wrapObject(null, renderView(view, DataQuery.of(), result));
  }
}
