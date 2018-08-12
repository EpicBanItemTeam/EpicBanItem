package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
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
    public CheckResult check(ItemStack item, World world, String trigger, @Nullable Subject subject) {
        return item.isEmpty() ? CheckResult.empty() : check(item.getType(), NbtTagDataUtil.toNbt(item), world, trigger, subject);
    }

    @Override
    public CheckResult check(ItemStackSnapshot item, World world, String trigger, @Nullable Subject subject) {
        return item.isEmpty() ? CheckResult.empty() : check(item.getType(), NbtTagDataUtil.toNbt(item), world, trigger, subject);
    }

    private CheckResult check(ItemType itemType, DataView itemStack, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty();
        getCheckRules(itemType).forEach(checkRule -> checkRule.check(itemStack, result, world, trigger, subject));
        return result;
    }

    public Map<ItemType, List<CheckRule>> getRules() {
        return rules;
    }

    public void setRules(Map<ItemType, List<CheckRule>> rules) {
        this.rules = rules;
    }

    public void clear() {
        rules = new HashMap<>();
    }

    @Override
    public void addRule(ItemType type, CheckRule rule) {
        List<CheckRule> ruleList;
        if (rules.containsKey(type)) {
            ruleList = rules.get(type);
            //check multi name
            for (CheckRule rule1 : ruleList) {
                if (rule1.getName().equals(rule.getName())) {
                    throw new IllegalArgumentException("Rule with the same name already exits");
                }
            }
        } else {
            ruleList = Lists.newArrayList();
            rules.put(type, ruleList);
        }
        ruleList.add(rule);
        ruleList.sort(Comparator.comparingInt(CheckRule::getPriority));
        try {
            EpicBanItem.plugin.getBanConfig().addRule(type, rule);
        } catch (IOException | ObjectMappingException e) {
            EpicBanItem.logger.error("Failed to save ban config.", e);
            throw new RuntimeException("Failed to save ban config.", e);
        }
    }

    public void addRules(Map<ItemType, List<CheckRule>> addRules) {
        for (Map.Entry<ItemType, List<CheckRule>> entry : addRules.entrySet()) {
            if (rules.containsKey(entry.getKey())) {
                List<CheckRule> origin = rules.get(entry.getKey());
                l1:
                for (CheckRule addRule : entry.getValue()) {
                    for (CheckRule originRule : origin) {
                        if (originRule.getName().equals(addRule.getName())) {
                            //todo:处理重复
                            continue l1;
                        }
                    }
                    origin.add(addRule);
                }
                origin.sort(Comparator.comparingInt(CheckRule::getPriority));
            } else {
                List<CheckRule> ruleList = new ArrayList<>(entry.getValue());
                ruleList.sort(Comparator.comparingInt(CheckRule::getPriority));
                rules.put(entry.getKey(), ruleList);
            }
        }
    }

}
