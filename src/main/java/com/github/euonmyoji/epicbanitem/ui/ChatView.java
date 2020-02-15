package com.github.euonmyoji.epicbanitem.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class ChatView {
    private int height;

    private List<TextLine> header;
    private VariableHeightLines context;
    private List<TextLine> footer;

    public ChatView(int height, List<TextLine> header, VariableHeightLines context, List<TextLine> footer) {
        this.height = height;
        this.header = header;
        this.context = context;
        this.footer = footer;
    }

    public List<Text> getLines(Player viewer) {
        List<Text> head = header.stream().map(l -> l.getLine(viewer)).collect(Collectors.toList());
        List<Text> foot = footer.stream().map(l -> l.getLine(viewer)).collect(Collectors.toList());
        List<Text> context = this.context.getLines(viewer, height - head.size() - foot.size());
        List<Text> r = new ArrayList<>();
        r.addAll(head);
        r.addAll(context);
        r.addAll(foot);
        return r;
    }

    public void showTo(Player player) {
        player.sendMessages(getLines(player));
    }
}
