package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
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
        DataContainer nbt = NbtTagDataUtil.toNbt(item);
        CheckResult checkResult = CheckResult.empty(nbt);
        return item.isEmpty() ? checkResult : check(checkResult, NbtTagDataUtil.getId(nbt), world, trigger, subject);
    }

    @Override
    public CheckResult check(ItemStackSnapshot item, World world, String trigger, @Nullable Subject subject) {
        DataContainer nbt = NbtTagDataUtil.toNbt(item);
        CheckResult checkResult = CheckResult.empty(nbt);
        return item.isEmpty() ? checkResult : check(checkResult, NbtTagDataUtil.getId(nbt), world, trigger, subject);
    }

    @Override
    public CheckResult check(BlockSnapshot block, World world, String trigger, @Nullable Subject subject) {
        DataContainer nbt = NbtTagDataUtil.toNbt(block);
        CheckResult checkResult = CheckResult.empty(nbt);
        boolean isAir = BlockTypes.AIR.equals(block.getState().getType());
        return isAir ? checkResult : check(checkResult, NbtTagDataUtil.getId(nbt), world, trigger, subject);
    }

    private CheckResult check(CheckResult origin, String id, World world, String trigger, @Nullable Subject subject) {
        return EpicBanItem.getBanConfig().getRulesWithIdFiltered(id).stream()
                .<UnaryOperator<CheckResult>>map(rule -> result -> rule.check(result, world, trigger, subject))
                .reduce(UnaryOperator.identity(), (f1, f2) -> result -> f2.apply(f1.apply(result))).apply(origin);
    }
}
