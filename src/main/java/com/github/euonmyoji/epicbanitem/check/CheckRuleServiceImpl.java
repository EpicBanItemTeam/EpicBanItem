package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CheckRuleServiceImpl implements CheckRuleService {
    @Override
    public void addRule(@Nullable ItemType type, CheckRule rule) {
        try {
            EpicBanItem.getBanConfig().addRule(type, rule);
        } catch (IOException e) {
            EpicBanItem.getLogger().error("Failed to save ban config.", e);
        }
    }

    @Override
    public boolean removeRule(String name) {
        try {
            return EpicBanItem.getBanConfig().removeRule(name);
        } catch (IOException e) {
            EpicBanItem.getLogger().error("Failed to save ban config.", e);
        }
        return false;
    }

    @Override
    public Set<ItemType> getCheckItemTypes() {
        return EpicBanItem.getBanConfig().getItems();
    }

    @Override
    public List<CheckRule> getCheckRules(@Nullable ItemType itemType) {
        return EpicBanItem.getBanConfig().getRules(itemType);
    }

    @Override
    public Collection<CheckRule> getCheckRules() {
        return EpicBanItem.getBanConfig().getRules();
    }

    @Override
    public Set<String> getRuleNames() {
        return EpicBanItem.getBanConfig().getRuleNames();
    }

    @Override
    public Optional<CheckRule> getCheckRule(String name) {
        return EpicBanItem.getBanConfig().getRule(name);
    }

    @Override
    public Optional<CheckRule> getCheckRule(@Nullable ItemType itemType, String name) {
        return getCheckRules(itemType).stream().filter(c -> c.getName().equals(name)).findFirst();
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
        return Stream.concat(getCheckRules(null).stream(), getCheckRules(itemType).stream())
                .<UnaryOperator<CheckResult>>map(rule -> result -> rule.check(result, world, trigger, subject))
                .reduce(UnaryOperator.identity(), (f1, f2) -> result -> f2.apply(f1.apply(result))).apply(origin);
    }
}
