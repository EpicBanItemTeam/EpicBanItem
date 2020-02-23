package team.ebi.epicbanitem.ui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.function.Function;

public class TranslateLine implements TextLine {
    private UiTextElement element;
    private Function<Text, Text> treans;

    public TranslateLine(UiTextElement element, Function<Text, Text> treans) {
        this.element = element;
        this.treans = treans;
    }

    @Override
    public Text getLine(Player viewer) {
        return treans.apply(element.toText(viewer));
    }
}
