package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.api.CheckResult;
import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
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
@SuppressWarnings("unused because of api")
@NonnullByDefault
public interface CheckRuleService extends com.github.euonmyoji.epicbanitem.api.CheckRuleService {
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
     * @param index 检查规则索引值
     * @return 适用的规则
     */
    List<CheckRule> getCheckRulesByIndex(CheckRuleIndex index);

    /**
     * 返回一个物品对应的规则名的规则 or empty
     *
     * @param index 检查规则索引值
     * @param name  规则名
     * @return 检查规则
     */
    Optional<CheckRule> getCheckRuleByNameAndIndex(CheckRuleIndex index, String name);

    @Deprecated
    default CheckResult check(ItemStack itemStack, World world, String trigger, @Nullable Subject subject) {
        CheckRuleTrigger ruleTrigger = getTrigger(trigger, false).orElseThrow(() -> new IllegalStateException("request unregistered trigger " + trigger));
        return check(itemStack, world, ruleTrigger, subject);
    }

    @Deprecated
    default CheckResult check(ItemStackSnapshot itemStack, World world, String trigger, @Nullable Subject subject) {
        CheckRuleTrigger ruleTrigger = getTrigger(trigger, false).orElseThrow(() -> new IllegalStateException("request unregistered trigger " + trigger));
        return check(itemStack, world, ruleTrigger, subject);
    }

    @Deprecated
    default CheckResult check(BlockSnapshot blockSnapshot, World world, String trigger, @Nullable Subject subject) {
        CheckRuleTrigger ruleTrigger = getTrigger(trigger, false).orElseThrow(() -> new IllegalStateException("request unregistered trigger " + trigger));
        return check(blockSnapshot, world, ruleTrigger, subject);
    }

    /**
     * Add a rule to the service and save it in the default config.
     *
     * @param rule the rule to
     * @return <tt>true</tt> if a rule was added as a result of this call
     */
    CompletableFuture<Boolean> appendRule(CheckRule rule);

    /**
     * Remove the rule with the given name. if present .
     *
     * @param rule the rule to remove , if present.
     * @return <tt>true</tt> if a rule was removed as a result of this call
     */
    CompletableFuture<Boolean> removeRule(CheckRule rule);

    @Override
    <T extends Subject> CheckResult check(ItemStackSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject);

    @Override
    <T extends Subject> CheckResult check(BlockSnapshot snapshot, World world, CheckRuleTrigger trigger, @Nullable T subject);

    @Override
    <T extends Subject> CheckResult check(ItemStack stack, World world, CheckRuleTrigger trigger, @Nullable T subject);

    default <T extends Subject> Iterable<Tuple<CheckResult, Slot>> checkInventory(
        Inventory inventory,
        World world,
        CheckRuleTrigger trigger,
        @Nullable T subject
    ) {
        return StreamSupport
            .stream(inventory.slots().spliterator(), false)
            .filter(slot -> slot instanceof Slot)
            .map(slot -> (Slot) slot)
            .filter(slot -> slot.peek().isPresent())
            .map(slot -> slot.peek().map(itemStack -> Tuple.of(check(itemStack, world, trigger, subject), slot)).get())
            .collect(Collectors.toList());
    }

    default <T extends Subject> Iterable<Tuple<CheckResult, Slot>> checkInventory(
        Inventory inventory,
        World world,
        CheckRuleTrigger trigger,
        CheckRule checkRule,
        @Nullable T subject
    ) {
        return StreamSupport
            .stream(inventory.slots().spliterator(), false)
            .filter(slot -> slot instanceof Slot)
            .map(slot -> (Slot) slot)
            .filter(slot -> slot.peek().isPresent())
            .map(
                slot ->
                    slot
                        .peek()
                        .map(
                            itemStack ->
                                Tuple.of(
                                    checkRule.check(CheckResult.empty(NbtTagDataUtil.toNbt(itemStack)), world, trigger, subject),
                                    slot
                                )
                        )
                        .get()
            )
            .collect(Collectors.toList());
    }
}
