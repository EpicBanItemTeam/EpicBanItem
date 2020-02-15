package com.github.euonmyoji.epicbanitem.ui;

import java.util.function.Function;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

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
