package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//填充命令用

/**
 * @author EBI TEAM
 */
@NonnullByDefault
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
    List<CheckRule> getCheckRules(@Nullable ItemType itemType);

    /**
     * 返回一个物品对应的规则名的规则 or empty
     *
     * @param itemType 物品类型
     * @param name     规则名
     * @return 检查规则
     */
    Optional<CheckRule> getCheckRule(@Nullable ItemType itemType, String name);

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

    /**
     * 检查一个物品并返回一个result
     *
     * @param itemStack 被检查的物品
     * @param world     检查发生世界
     * @param trigger   触发器
     * @param subject   subject
     * @return 检查结果
     */
    CheckResult check(ItemStackSnapshot itemStack, World world, String trigger, @Nullable Subject subject);

    /**
     * Add a rule to the service and save it in the default config.
     *
     * @param type item type of the rule
     * @param rule the rule to add
     *             // TODO: throws exceptions
     */
    void addRule(ItemType type, CheckRule rule);

}
