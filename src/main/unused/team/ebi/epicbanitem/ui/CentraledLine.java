package team.ebi.epicbanitem.ui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author The EpicBanItem Team
 */
public class CentraledLine implements TextLine {
    private int width;
    private List<UiTextElement> elements;

    @Nullable
    private Text fill;

    public CentraledLine(int width, List<UiTextElement> elements, @Nullable Text fill) {
        this.width = width;
        this.elements = elements;
        this.fill = fill;
    }

    @Override
    public Text getLine(Player viewer) {
        List<Text> texts = elements.stream().map(e -> e.toText(viewer)).collect(Collectors.toList());
        int cWidth = texts.stream().map(Text::toPlain).mapToInt(String::length).sum();
        if (cWidth >= width) {
            return Text.builder().append(texts.stream().map(TextRepresentable::toText).collect(Collectors.toList())).build();
        }
        int l = width - cWidth / 2;
        if (fill == null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < l; i++) {
                builder.append(" ");
            }
            return Text.builder(builder.toString()).append(texts).build();
        } else {
            String s = fill.toPlain();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < l; i++) {
                builder.append(s);
            }
            Text t = Text.builder(fill, builder.toString()).toText();
            return Text.builder().append(t).append(texts).append(t).build();
        }
    }
}
