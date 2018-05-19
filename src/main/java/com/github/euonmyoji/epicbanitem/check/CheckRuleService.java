package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

//填充命令用

/**
 * @author EpicBanItem Team
 */
public interface CheckRuleService {
    //Map<ItemType,Map<String,CheckRule>>?
    //Map<ItemType,Collection<CheckRule>>?

    /**
     * 返回会被检查的物品类型
     *
     * @return ItemTypes
     */
    Set<ItemType> getCheckItemTypes();

    /**
     * 返回一个物品适用的规则 or empty
     *
     * @param itemType 物品类型
     * @return 适用的规则
     */
    Collection<CheckRule> getCheckRules(ItemType itemType);

    /**
     * 返回一个物品对应的规则名的规则 or empty
     *
     * @param itemType 物品类型
     * @param name     规则名
     * @return 检查规则
     */
    Optional<CheckRule> getCheckRule(ItemType itemType, String name);

    /**
     * 检查一个物品并返回一个result
     *
     * @param itemStack 被检查的物品
     * @param world     检查发生世界
     * @param trigger   触发器
     * @param subject   subject
     * @return 检查结果
     */
    CheckResult check(ItemStack itemStack, World world, String trigger, @Nullable Subject subject);

}
