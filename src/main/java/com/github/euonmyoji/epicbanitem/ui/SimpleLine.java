package com.github.euonmyoji.epicbanitem.ui;

import java.util.List;
import java.util.stream.Collectors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class SimpleLine implements TextLine {
    private List<UiTextElement> texts;

    public SimpleLine(List<UiTextElement> texts) {
        this.texts = texts;
    }

    public Text getLine(Player viewer) {
        return Text.builder().append(texts.stream().map(e -> e.toText(viewer)).collect(Collectors.toList())).build();
    }
}
