package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.World;

//填充命令用
public interface CheckRule<T> {
    String getName();

    ItemType getItemType();

    T check(T obj, World world);
}
