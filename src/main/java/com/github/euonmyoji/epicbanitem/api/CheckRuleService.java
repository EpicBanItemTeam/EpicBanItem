package com.github.euonmyoji.epicbanitem.api;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import javax.annotation.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public interface CheckRuleService {
    /**
     * 获取实例
     *
     * @return 实例
     */
    static CheckRuleService instance() {
        return Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
    }

    /**
     * 检查一个物品并返回一个 result
     *
     * @param snapshot 被检查的物品
     * @param world    检查发生世界
     * @param trigger  触发器
     * @param subject  用于检查权限的对象
     * @return 检查结果
     */
    <T extends Subject> CheckResult check(ItemStackSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject);

    /**
     * 检查一个方块并返回一个 result
     *
     * @param snapshot 被检查的方块
     * @param world    检查发生世界
     * @param trigger  触发器
     * @param subject  用于检查权限的对象
     * @return 检查结果
     */
    <T extends Subject> CheckResult check(BlockSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject);

    /**
     * 检查一个物品并返回一个 result
     *
     * @param stack   被检查的物品
     * @param world   检查发生世界
     * @param trigger 触发器
     * @param subject 用于检查权限的对象
     * @return 检查结果
     */
    <T extends Subject> CheckResult check(ItemStack stack, World world, CheckRuleTrigger trigger, @Nullable T subject);

    /**
     * 检查一个库存并返回 result
     *
     * @param inventory  被检查的库存
     * @param world      检查发生世界
     * @param trigger    触发器
     * @param subject    用于检查权限的对象
     * @return 检查结果和相应 {@link org.spongepowered.api.item.inventory.Slot}
     */
    <T extends Subject> Iterable<Tuple<CheckResult, Slot>> checkInventory(
        Inventory inventory,
        World world,
        CheckRuleTrigger trigger,
        @Nullable T subject
    );

    /**
     * 检查一个库存并返回 result
     *
     * @param inventory  被检查的库存
     * @param world      检查发生世界
     * @param trigger    触发器
     * @param checkRule  用于检查的规则
     * @param subject    用于检查权限的对象
     * @return 检查结果和相应 {@link org.spongepowered.api.item.inventory.Slot}
     */
    <T extends Subject> Iterable<Tuple<CheckResult, Slot>> checkInventory(
        Inventory inventory,
        World world,
        CheckRuleTrigger trigger,
        CheckRule checkRule,
        @Nullable T subject
    );

}
