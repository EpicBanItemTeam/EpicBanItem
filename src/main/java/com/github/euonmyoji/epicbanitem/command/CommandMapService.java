package com.github.euonmyoji.epicbanitem.command;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.spongepowered.api.command.CommandCallable;

@Singleton
public class CommandMapService {
    private Map<List<String>, CommandCallable> childrenMap = Maps.newHashMap();
    private Map<String, CommandCallable> flatMap;

    public CommandMapService registerCommand(ICommand command) {
        childrenMap.put(command.getNameList(), command.getCallable());
        return this;
    }

    public Map<List<String>, CommandCallable> getChildrenMap() {
        return childrenMap;
    }

    public void forEach(BiConsumer<? super List<String>, ? super CommandCallable> action) {
        childrenMap.forEach(action);
    }

    public Map<String, CommandCallable> getFlatMap() {
        if (flatMap == null) {
            flatMap = Maps.newHashMap();
            childrenMap.forEach((commands, commandCallable) -> commands.forEach(command -> flatMap.put(command, commandCallable)));
        }
        return flatMap;
    }
}
