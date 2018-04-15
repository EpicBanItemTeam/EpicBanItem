package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.Set;

//填充命令用
public interface CheckRuleService {
    //Map<ItemType,Map<String,CheckRule>>?
    //Map<ItemType,Collection<CheckRule>>?

    Set<ItemType> getCheckingItems();

    Collection<CheckRule> getCheckRules(ItemType itemType);

}
