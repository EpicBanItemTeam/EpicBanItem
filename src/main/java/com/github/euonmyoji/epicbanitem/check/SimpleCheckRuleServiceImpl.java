package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author EpicBanItem Team
 */
@NonnullByDefault
public class SimpleCheckRuleServiceImpl implements CheckRuleService {
    @Override
    public void addRule(ItemType type, CheckRule rule) {
        try {
            EpicBanItem.plugin.getBanConfig().addRule(type, rule);
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to save ban config.", e);
        }
    }

    @Override
    public Set<ItemType> getCheckItemTypes() {
        return EpicBanItem.plugin.getBanConfig().getItems();
    }

    @Override
    public List<CheckRule> getCheckRules(ItemType itemType) {
        return EpicBanItem.plugin.getBanConfig().getRules(itemType);
    }

    @Override
    public Optional<CheckRule> getCheckRule(ItemType itemType, String name) {
        return getCheckRules(itemType).stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    @Override
    public CheckResult check(ItemStack item, World world, String trigger, @Nullable Subject subject) {
        if (item.isEmpty()) {
            return CheckResult.empty();
        } else {
            return check(item.getType(), NbtTagDataUtil.toNbt(item), world, trigger, subject);
        }
    }

    @Override
    public CheckResult check(ItemStackSnapshot item, World world, String trigger, @Nullable Subject subject) {
        if (item.isEmpty()) {
            return CheckResult.empty();
        } else {
            return check(item.getType(), NbtTagDataUtil.toNbt(item), world, trigger, subject);
        }
    }

    private CheckResult check(ItemType type, DataView view, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty();
        for (CheckRule checkRule : getCheckRules(type)) {
            checkRule.check(view, result, world, trigger, subject);
        }
        return result;
    }
}
