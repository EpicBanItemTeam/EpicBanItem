package com.github.euonmyoji.epicbanitem.ui;

import com.github.euonmyoji.epicbanitem.command.CommandCallback;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

@NonnullByDefault
public class SwitchButton<T> extends Button {
    private int index;
    private List<T> stats;
    private Consumer<T> update;

    @Nullable
    private Function<T, Text> hover;

    public SwitchButton(Supplier<Text.Builder> display, T origin, List<T> stats, Consumer<T> update, @Nullable Function<T, Text> hover) {
        super(display);
        this.hover = hover;
        for (int i = 0; i < stats.size(); i++) {
            if (stats.get(i).equals(origin)) {
                this.index = i;
                break;
            }
        }
        this.stats = stats;
        this.update = update;
    }

    @Override
    public void onClick(CommandSource source) {
        index++;
        if (index >= stats.size()) {
            index = 0;
        }
        update.accept(stats.get(index));
    }

    private T peakNext() {
        int next = index + 1;
        if (next >= stats.size()) {
            next = 0;
        }
        return stats.get(next);
    }

    @Override
    public Text toText(Player viewer) {
        Text.Builder builder = display.get().onClick(CommandCallback.addCallback(viewer.getUniqueId(), this::onClick));
        if (hover != null) {
            Text h = hover.apply(peakNext());
            TextUtil.addHoverMessage(builder, h);
        }
        return builder.build();
    }
}
