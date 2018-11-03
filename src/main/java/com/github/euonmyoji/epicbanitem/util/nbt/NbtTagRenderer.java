package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.base.Strings;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author ustc_zzzz
 */
public class NbtTagRenderer {
    private static final int INDENT = 2;

    private final QueryResult queryResult;

    public NbtTagRenderer(QueryResult result) {
        this.queryResult = result;
    }

    public Text render(DataView view) {
        return this.render(view, this.queryResult, INDENT);
    }

    private Text render(Object view, @Nullable QueryResult result, int indent) {
        Map<String, Object> map = NbtTypeHelper.getAsMap(view);
        if (Objects.nonNull(map)) {
            String separator = "\n";
            Text.Builder builder = Text.builder();
            boolean isResultNull = Objects.isNull(result);
            Map<String, QueryResult> children = isResultNull ? Collections.emptyMap() : result.getChildren();
            builder.append(isResultNull ? Text.of("{") : Text.of(TextColors.GREEN, "{"));
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (children.containsKey(key)) {
                    Text childrenText = this.render(entry.getValue(), children.get(key), indent + INDENT);
                    Text prefix = Text.of(TextColors.GREEN, Strings.repeat(" ", indent), key, ": ");
                    builder.append(Text.of(separator)).append(prefix);
                    builder.append(childrenText);
                    separator = ", \n";
                } else {
                    Text childrenText = this.render(entry.getValue(), null, indent + INDENT);
                    Text prefix = Text.of(Strings.repeat(" ", indent), key, ": ");
                    builder.append(Text.of(separator)).append(prefix);
                    builder.append(childrenText);
                    separator = ", \n";
                }
            }
            builder.append(Text.of("\n", Strings.repeat(" ", indent - INDENT)));
            return builder.append(isResultNull ? Text.of("}") : Text.of(TextColors.GREEN, "}")).build();
        }
        List<Object> list = NbtTypeHelper.getAsList(view);
        if (Objects.nonNull(list)) {
            String separator = "\n";
            Text.Builder builder = Text.builder();
            boolean isResultNull = Objects.isNull(result);
            Map<String, QueryResult> children = isResultNull ? Collections.emptyMap() : result.getChildren();
            builder.append(isResultNull ? Text.of("[") : Text.of(TextColors.GREEN, "["));
            for (int i = 0; i < list.size(); ++i) {
                String key = Integer.toString(i);
                if (children.containsKey(key)) {
                    Text childrenText = this.render(list.get(i), children.get(key), indent + INDENT);
                    Text prefix = Text.of(TextColors.GREEN, Strings.repeat(" ", indent));
                    builder.append(Text.of(separator)).append(prefix);
                    builder.append(childrenText);
                    separator = ", \n";
                } else {
                    Text childrenText = this.render(list.get(i), null, indent + INDENT);
                    Text prefix = Text.of(Strings.repeat(" ", indent));
                    builder.append(Text.of(separator)).append(prefix);
                    builder.append(childrenText);
                    separator = ", \n";
                }
            }
            builder.append(Text.of("\n", Strings.repeat(" ", indent - INDENT)));
            return builder.append(isResultNull ? Text.of("]") : Text.of(TextColors.GREEN, "]")).build();
        }
        return Text.of(NbtTypeHelper.toString(view));
    }
}
