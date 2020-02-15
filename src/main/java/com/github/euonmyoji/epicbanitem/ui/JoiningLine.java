package com.github.euonmyoji.epicbanitem.ui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class JoiningLine implements TextLine {
    private List<UiTextElement> elements;
    private UiTextElement joining;

    public JoiningLine(List<UiTextElement> elements, UiTextElement joining) {
        this.elements = elements;
        this.joining = joining;
    }

    @Override
    public Text getLine(Player viewer) {
        return Text.joinWith(joining.toText(viewer), elements.stream().map(e->e.toText(viewer)).collect(Collectors.toList()));
    }
}
