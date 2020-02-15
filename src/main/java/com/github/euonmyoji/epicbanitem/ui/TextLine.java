package com.github.euonmyoji.epicbanitem.ui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@FunctionalInterface
public interface TextLine {
    Text getLine(Player viewer);
}
