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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author EpicBanItem Team
 */
@NonnullByDefault
public class CheckRuleServiceImpl implements CheckRuleService {
    @Override
    public void addRule(@Nullable ItemType type, CheckRule rule) {
        try {
            EpicBanItem.plugin.getBanConfig().addRule(type, rule);
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to save ban config.", e);
        }
    }

    @Override
    public boolean removeRule(String name) {
        try {
            return EpicBanItem.plugin.getBanConfig().removeRule(name);
        } catch (IOException e) {
            EpicBanItem.logger.error("Failed to save ban config.", e);
        }
        return false;
    }

    @Override
    public Set<ItemType> getCheckItemTypes() {
        return EpicBanItem.plugin.getBanConfig().getItems();
    }

    @Override
    public List<CheckRule> getCheckRules(@Nullable ItemType itemType) {
        return EpicBanItem.plugin.getBanConfig().getRules(itemType);
    }

    @Override
    public Collection<CheckRule> getCheckRules() {
        return EpicBanItem.plugin.getBanConfig().getRules();
    }

    @Override
    public Set<String> getRuleNames() {
        return EpicBanItem.plugin.getBanConfig().getRuleNames();
    }

    @Override
    public Optional<CheckRule> getCheckRule(String name) {
        return EpicBanItem.plugin.getBanConfig().getRule(name);
    }

    @Override
    public Optional<CheckRule> getCheckRule(@Nullable ItemType itemType, String name) {
        return getCheckRules(itemType).stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    @Override
    public CheckResult check(ItemStack item, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty();
        if (!item.isEmpty()) {
            check(result, item.getType(), NbtTagDataUtil.toNbt(item), world, trigger, subject);
        }
        return result;
    }

    @Override
    public CheckResult check(ItemStackSnapshot item, World world, String trigger, @Nullable Subject subject) {
        CheckResult result = CheckResult.empty();
        if (!item.isEmpty()) {
            check(result, item.getType(), NbtTagDataUtil.toNbt(item), world, trigger, subject);
        }
        return result;
    }

    private void check(CheckResult result, ItemType type, DataView view, World world, String trigger, @Nullable Subject subject) {
        Stream<CheckRule> rules = Stream.concat(getCheckRules(null).stream(), getCheckRules(type).stream());
        rules.forEach(rule -> rule.check(view, result, world, trigger, subject));
    }
}
