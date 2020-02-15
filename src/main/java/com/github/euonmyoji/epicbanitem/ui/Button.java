package com.github.euonmyoji.epicbanitem.ui;

import com.github.euonmyoji.epicbanitem.command.CommandCallback;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.function.Supplier;

@NonnullByDefault
public abstract class Button implements UiTextElement {
    protected Supplier<Text.Builder> display;

    protected Button(Supplier<Text.Builder> display) {
        this.display = display;
    }

    public abstract void onClick(CommandSource source);

    @Override
    public Text toText(Player viewer) {
        return display.get().onClick(CommandCallback.addCallback(viewer.getUniqueId(), this::onClick)).build();
    }
}
