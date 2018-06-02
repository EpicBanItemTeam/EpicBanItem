package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class SimpleCheckRuleServiceImpl implements CheckRuleService {
    private Map<ItemType, Map<String, CheckRule>> rules = new HashMap<>();

    @Override
    public Set<ItemType> getCheckItemTypes() {
        return rules.keySet();
    }

    @Override
    public Collection<CheckRule> getCheckRules(ItemType itemType) {
        return rules.get(itemType).values();
    }

    @Override
    public Optional<CheckRule> getCheckRule(ItemType itemType, String name) {
        return Optional.ofNullable(rules.get(itemType).get(name));
    }

    @Override
    public CheckResult check(ItemStack itemStack, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty();
        getCheckRules(itemStack.getType()).forEach(checkRule -> checkRule.check(itemStack, result,
                world, trigger, subject));
        return result;
    }

    public void reload() {
        //todo:如果无法正常载入新的配置 要不要恢复为之前的
    }
}
