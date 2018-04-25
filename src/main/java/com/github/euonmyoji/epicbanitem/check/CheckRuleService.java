package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

//填充命令用
public interface CheckRuleService {
    //Map<ItemType,Map<String,CheckRule>>?
    //Map<ItemType,Collection<CheckRule>>?

    Set<ItemType> getCheckItemTypes();

    Collection<CheckRule> getCheckRules(ItemType itemType);

    Optional<CheckRule> getCheckRule(ItemType itemType, String name);

    CheckResult check(ItemStack itemStack, World world, String trigger, @Nullable Subject subject);

    CheckResult check(ItemStack itemStack, @Nullable Player p, String trigger);
}
