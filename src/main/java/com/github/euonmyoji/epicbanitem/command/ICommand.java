package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandCallable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public interface ICommand {
    CommandCallable getCallable();

    String getName();

    String[] getAlias();

    default List<String> getNameList() {
        return Stream.concat(Stream.of(getName()), Arrays.stream(getAlias())).collect(Collectors.toList());
    }
}
