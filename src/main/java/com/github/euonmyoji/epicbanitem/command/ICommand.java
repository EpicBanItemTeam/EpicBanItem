package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandCallable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public interface ICommand {
    CommandCallable getCallable();

    String getName();

    String[] getAlias();

    default List<String> getNameList() {
        List<String> list = new ArrayList<>();
        list.add(getName());
        list.addAll(Arrays.asList(getAlias()));
        return list;
    }
}
