package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface CheckRuleService {
    /**
     * return name set of all active rules
     *
     * @return 适用的规则
     */
    Set<String> getNames();

    /**
     * 返回会被检查的物品类型
     *
     * @return ItemTypes
     */
    Set<CheckRuleIndex> getIndexes();

    /**
     * return all active rules
     *
     * @return 适用的规则
     */
    Collection<CheckRule> getCheckRules();

    /**
     * get check rule for the name or empty
     *
     * @param name 规则名
     * @return 检查规则
     */
    Optional<CheckRule> getCheckRuleByName(String name);

    /**
     * 返回一个物品适用的规则 or empty
     *
     * @param itemType 物品类型
     * @return 适用的规则
     */
    List<CheckRule> getCheckRulesByIndex(CheckRuleIndex index);

    /**
     * 返回一个物品对应的规则名的规则 or empty
     *
     * @param itemType 物品类型
     * @param name     规则名
     * @return 检查规则
     */
    Optional<CheckRule> getCheckRuleByNameAndIndex(CheckRuleIndex index, String name);

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
     * 检查一个物品并返回一个result
     *
     * @param blockSnapshot 被检查的Block
     * @param world         检查发生世界
     * @param trigger       触发器
     * @param subject       subject
     * @return 检查结果
     */
    CheckResult check(BlockSnapshot blockSnapshot, World world, String trigger, @Nullable Subject subject);

    /**
     * Add a rule to the service and save it in the default config.
     *
     * @param type item type of the rule
     * @param rule the rule to
     *
     * @return <tt>true</tt> if a rule was added as a result of this call
     */
    CompletableFuture<Boolean> appendRule(CheckRule rule);

    /**
     * Remove the rule with the given name. if present .
     *
     * @param name the name of the rule to remove , if present.
     * @return <tt>true</tt> if a rule was removed as a result of this call
     */
    CompletableFuture<Boolean> removeRule(CheckRule rule);
}
