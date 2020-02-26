package team.ebi.epicbanitem.util.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author The EpicBanItem Team
 */
public class NbtTagRenderer {
    public static final NbtTagRenderer EMPTY_RENDERER = new NbtTagRenderer(null);
    private static final int INDENT = 2;
    private final QueryResult queryResult;

    public NbtTagRenderer(QueryResult result) {
        this.queryResult = result;
    }

    public List<Text> render(DataView view) {
        return this.render(view, this.queryResult, 0, "", "");
    }

    private List<Text> render(Object view, @Nullable QueryResult result, int indent, String path, String hoverPath) {
        Map<String, Object> map = NbtTypeHelper.getAsMap(view);
        List<Object> list = NbtTypeHelper.getAsList(view);
        List<Text> texts = Lists.newArrayList();
        boolean isTarget = Objects.isNull(result);
        Text indentText = Text.of(TextColors.DARK_GRAY, Strings.repeat(" │", Math.max(indent / 2 - 1, 0)), Strings.repeat(" ", indent > 1 ? 1 : 0));
        Map<String, QueryResult> childResult = isTarget ? Collections.emptyMap() : result.getChildren();

        if (Objects.nonNull(map)) {
            map.forEach(
                (key, value) -> {
                    List<String> pathList = Lists.newArrayList();
                    if (!Strings.isNullOrEmpty(path)) {
                        pathList.addAll(Arrays.asList(path.split("\\.")));
                    }
                    pathList.add(key);
                    String currentPath = String.join(".", pathList);

                    pathList.clear();
                    if (!Strings.isNullOrEmpty(hoverPath)) {
                        pathList.addAll(Arrays.asList(hoverPath.split("\\.")));
                    }
                    pathList.add(key);

                    String newHoverPath = String.join(".", pathList);

                    Text.Builder tagNameBuilder = Text
                        .builder()
                        .append(indentText)
                        .onHover(TextActions.showText(Text.of(newHoverPath)))
                        .onClick(TextActions.suggestCommand(currentPath));

                    Text tagName;
                    List<Text> childrenText;

                    if (childResult.containsKey(key)) {
                        childrenText = this.render(value, childResult.get(key), indent + INDENT, currentPath, newHoverPath);
                        tagNameBuilder.color(TextColors.AQUA).style(TextStyles.BOLD);
                    } else {
                        childrenText = this.render(value, null, indent + INDENT, currentPath, newHoverPath);
                    }

                    tagName = tagNameBuilder.append(Text.of(key, ": ")).build();

                    if (!childrenText.isEmpty() && !childrenText.get(0).toPlain().startsWith(" ")) {
                        texts.add(tagName.concat(childrenText.get(0)));
                    } else {
                        texts.add(tagName);
                        texts.addAll(childrenText);
                    }
                }
            );
        } else if (Objects.nonNull(list)) {
            for (int i = 0; i < list.size(); ++i) {
                String key = Integer.toString(i);

                List<String> paths = Lists.newArrayList();
                if (!Strings.isNullOrEmpty(path)) {
                    paths.addAll(Arrays.asList(path.split("\\.")));
                }

                String newHoverPath = String.join(".", paths);
                newHoverPath += "[" + key + "]";

                Text.Builder tagNameBuilder = Text.builder().append(indentText);

                List<Text> childrenText;
                Text tagName;

                if (childResult.containsKey(key)) {
                    childrenText = this.render(list.get(i), childResult.get(key), indent + INDENT * 2, path, newHoverPath);
                    tagNameBuilder.color(TextColors.AQUA).style(TextStyles.BOLD);
                } else {
                    childrenText = this.render(list.get(i), null, indent + INDENT * 2, path, newHoverPath);
                }

                tagName = tagNameBuilder.append(Text.of("[" + key + "]: ")).build();

                if (childrenText.size() == 1) {
                    texts.add(tagName.concat(childrenText.get(0)));
                } else {
                    texts.add(tagName);
                    texts.addAll(childrenText);
                }
            }
        } else {
            String stringValue = NbtTypeHelper.toString(view);
            Text.Builder builder = Text.builder(stringValue);
            if (view instanceof Boolean) {
                builder.color(TextColors.LIGHT_PURPLE);
            } else if (view instanceof String) {
                builder.color(TextColors.GREEN);
            } else {
                builder.color(TextColors.GOLD);
            }

            // TODO: 2020/2/13 需要本地化？
            builder.onHover(TextActions.showText(Text.of(view.getClass().getSimpleName())));
            builder.onClick(TextActions.suggestCommand(stringValue));

            texts.add(builder.build());
        }

        return texts;
    }
}
