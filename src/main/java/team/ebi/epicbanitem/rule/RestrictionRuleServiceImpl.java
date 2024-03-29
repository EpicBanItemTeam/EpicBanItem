/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.rule;

import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import team.ebi.epicbanitem.api.rule.*;

@Singleton
public class RestrictionRuleServiceImpl implements RestrictionRuleService {

    private final BiMap<ResourceKey, RestrictionRule> map = HashBiMap.create();

    @Inject
    private RulePredicateService predicateService;

    @Inject
    private Injector injector;

    private final Supplier<RestrictionRulesStorage> rulesStorage =
            Suppliers.memoize(() -> injector.getInstance(RestrictionRulesStorage.class));

    @Inject
    public RestrictionRuleServiceImpl() {
        Sponge.dataManager().registerBuilder(RestrictionRule.class, new RestrictionRuleImpl.Builder());
        Sponge.dataManager().registerBuilder(TriggerStates.class, new TriggerStates.Builder());
        Sponge.dataManager().registerBuilder(WorldStates.class, new WorldStates.Builder());
    }

    @Override
    public Optional<ResourceKey> register(ResourceKey key, RestrictionRule rule) {
        RestrictionRule putted = map.forcePut(key, rule);
        if (putted != null) {
            predicateService.remove(putted);
        }
        predicateService.register(rule);
        return Optional.of(key);
    }

    @Override
    public ImmutableBiMap<ResourceKey, RestrictionRule> all() {
        return ImmutableBiMap.copyOf(map);
    }

    @Override
    public void clear() {
        predicateService.clear();
        map.clear();
    }

    @Override
    public RestrictionRule remove(ResourceKey key) {
        RestrictionRule rule = map.remove(key);
        predicateService.remove(rule);
        rulesStorage.get().remove(key);
        return rule;
    }

    @Override
    public void save() {
        rulesStorage.get().save();
    }

    @Override
    public void save(ResourceKey key) {
        rulesStorage.get().save(key);
    }
}
