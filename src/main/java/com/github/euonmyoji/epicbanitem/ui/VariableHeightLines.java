package com.github.euonmyoji.epicbanitem.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class VariableHeightLines {
    private List<TextLine> lines;

    public VariableHeightLines(List<TextLine> lines) {
        this.lines = lines;
    }

    public List<Text> getLines(Player player, int height) {
        List<Text> lines = this.lines.stream().map(l -> l.getLine(player)).collect(Collectors.toList());
        if (height <= lines.size()) {
            return lines;
        }
        int l = (height - lines.size()) / 2;
        List<Text> result = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            result.add(Text.EMPTY);
        }
        result.addAll(lines);
        for (int i = 0; i < height - lines.size() - l; i++) {
            result.add(Text.EMPTY);
        }
        return result;
    }
}
