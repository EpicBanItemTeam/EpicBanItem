package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandCallable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public interface ICommand {
    /**
     * get the callable command
     *
     * @return callable command
     */
    CommandCallable getCallable();

    /**
     * get the name
     *
     * @return name
     */
    String getName();

    /**
     * get alias
     *
     * @return the alias
     */
    String[] getAlias();

    /**
     * get name list
     *
     * @return the list of the name
     */
    default List<String> getNameList() {
        return Stream.concat(Stream.of(getName()), Arrays.stream(getAlias())).collect(Collectors.toList());
    }
}
