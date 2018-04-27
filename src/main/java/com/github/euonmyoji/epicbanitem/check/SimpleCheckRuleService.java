package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class SimpleCheckRuleService implements CheckRuleService {
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
<<<<<<< HEAD
        return null;
    }

    @Override
    public CheckResult check(ItemStack itemStack, Player p, String trigger) {
        CheckResult result = CheckResult.empty();
        getCheckRules(itemStack.getType()).forEach(checkRule -> checkRule.check(itemStack, result,
                p.getWorld(), trigger, p));
=======
        CheckResult result = CheckResult.empty();
        getCheckRules(itemStack.getType()).forEach(checkRule -> checkRule.check(itemStack, result, world, trigger, subject));
>>>>>>> 7bab48494f308ee37f1f19ec80e88fba041db2ea
        return result;
    }

}
