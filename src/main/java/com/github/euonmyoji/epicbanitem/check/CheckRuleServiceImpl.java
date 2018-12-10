package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CheckRuleServiceImpl implements CheckRuleService {
    @Override
    public CompletableFuture<Boolean> appendRule(CheckRule rule) {
        try {
            return EpicBanItem.getBanConfig().addRule(CheckRuleIndex.of(rule.getQueryNode()), rule);
        } catch (IOException e) {
            EpicBanItem.getLogger().error("Failed to save ban config.", e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Boolean> removeRule(CheckRule rule) {
        try {
            return EpicBanItem.getBanConfig().removeRule(CheckRuleIndex.of(rule.getQueryNode()), rule.getName());
        } catch (IOException e) {
            EpicBanItem.getLogger().error("Failed to save ban config.", e);
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public Set<CheckRuleIndex> getIndexes() {
        return EpicBanItem.getBanConfig().getItems();
    }

    @Override
    public List<CheckRule> getCheckRulesByIndex(CheckRuleIndex index) {
        return EpicBanItem.getBanConfig().getRules(index);
    }

    @Override
    public Collection<CheckRule> getCheckRules() {
        return EpicBanItem.getBanConfig().getRules();
    }

    @Override
    public Set<String> getNames() {
        return EpicBanItem.getBanConfig().getRuleNames();
    }

    @Override
    public Optional<CheckRule> getCheckRuleByName(String name) {
        return EpicBanItem.getBanConfig().getRule(name);
    }

    @Override
    public Optional<CheckRule> getCheckRuleByNameAndIndex(CheckRuleIndex index, String name) {
        return EpicBanItem.getBanConfig().getRules(index).stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    @Override
    public CheckResult check(ItemStack item, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty(NbtTagDataUtil.toNbt(item));
        return item.isEmpty() ? result : check(result, item.getType(), world, trigger, subject);
    }

    @Override
    public CheckResult check(ItemStackSnapshot item, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty(NbtTagDataUtil.toNbt(item));
        return item.isEmpty() ? result : check(result, item.getType(), world, trigger, subject);
    }

    @Override
    public CheckResult check(BlockSnapshot snapshot, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty(NbtTagDataUtil.toNbt(snapshot));
        return check(result, snapshot.getState().getType().getItem().orElse(ItemTypes.AIR), world, trigger, subject);
    }

    private CheckResult check(CheckResult origin, ItemType itemType, World world, String trigger, @Nullable Subject subject) {
        CheckRuleIndex i = CheckRuleIndex.of(), j = CheckRuleIndex.of(itemType);
        List<List<CheckRule>> ruleLists = Arrays.asList(getCheckRulesByIndex(i), getCheckRulesByIndex(j));
        return Streams.stream(Iterables.mergeSorted(ruleLists, EpicBanItem.getBanConfig().getComparator()))
                .<UnaryOperator<CheckResult>>map(rule -> result -> rule.check(result, world, trigger, subject))
                .reduce(UnaryOperator.identity(), (f1, f2) -> result -> f2.apply(f1.apply(result))).apply(origin);
    }
}
