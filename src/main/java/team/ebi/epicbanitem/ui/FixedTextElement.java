package team.ebi.epicbanitem.ui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;

public class FixedTextElement implements UiTextElement, TextLine {
    private TextRepresentable text;

    public FixedTextElement(TextRepresentable text) {
        this.text = text;
    }

    @Override
    public Text toText(Player viewer) {
        return text.toText();
    }

    @Override
    public Text getLine(Player viewer) {
        return text.toText();
    }
}
