package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandCallable;

public interface ICommand {
    CommandCallable getCallable();
    String getName();
    String[] getAlias();
}
