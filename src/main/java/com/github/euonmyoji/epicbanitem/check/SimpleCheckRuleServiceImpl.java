package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author EpicBanItem Team
 */
public class SimpleCheckRuleServiceImpl implements CheckRuleService {
    private Map<ItemType, List<CheckRule>> rules = new HashMap<>();

    @Override
    public Set<ItemType> getCheckItemTypes() {
        return rules.keySet();
    }

    @Override
    public List<CheckRule> getCheckRules(ItemType itemType) {
        return rules.getOrDefault(itemType, Collections.emptyList());
    }

    @Override
    public Optional<CheckRule> getCheckRule(ItemType itemType, String name) {
        for (CheckRule rule : getCheckRules(itemType)) {
            if (rule.getName().equals(name)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    @Override
    public CheckResult check(ItemStack itemStack, World world, String trigger, @Nullable Subject subject) {
        return check(itemStack.getType(), NbtTagDataUtil.toNbt(itemStack), world, trigger, subject);
    }

    @Override
    public CheckResult check(ItemStackSnapshot itemStack, World world, String trigger, @Nullable Subject subject) {
        return check(itemStack.getType(), NbtTagDataUtil.toNbt(itemStack), world, trigger, subject);
    }

    public CheckResult check(ItemType itemType, DataView itemStack, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty();
        getCheckRules(itemType).forEach(checkRule -> checkRule.check(itemStack, result, world, trigger, subject));
        return result;
    }

    public void reload() {
        //todo:如果无法正常载入新的配置 要不要恢复为之前的
    }
}