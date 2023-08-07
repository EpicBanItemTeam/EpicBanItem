/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import java.util.*;
import java.util.stream.Stream;

import org.spongepowered.api.ResourceKey;

import com.google.common.collect.Lists;
import com.google.inject.ImplementedBy;
import it.unimi.dsi.fastutil.objects.*;
import team.ebi.epicbanitem.rule.RulePredicateServiceImpl;

@ImplementedBy(RulePredicateServiceImpl.class)
public interface RulePredicateService {

    ResourceKey WILDCARD = ResourceKey.of("_", "_");

    Comparator<RestrictionRule> PRIORITY_ASC = Comparator.comparingInt(RestrictionRule::priority);

    /**
     * @param id {@link ResourceKey} of object
     * @return Rules that match the predicates
     */
    default Stream<RestrictionRule> rules(ResourceKey id) {
        return this.rules(predicates(id));
    }

    default Stream<RestrictionRule> rules(Set<ResourceKey> predicates) {
        return predicates.stream().map(this::rule).flatMap(Collection::stream).distinct();
    }

    default ResourceKey minimumPredicate(Collection<ResourceKey> keys) {
        return keys.stream()
                .map(this::predicates)
                .reduce((s1, s2) -> {
                    s1.retainAll(s2);
                    return s1;
                })
                .map(SortedSet::last)
                .orElse(WILDCARD);
    }

    default ResourceKey minimumPredicate(ResourceKey key) {
        return predicates(key).last();
    }

    /**
     * @param id Object id
     * @return All possible predicates. Sorted for calc predicate from keys
     */
    default SortedSet<ResourceKey> predicates(ResourceKey id) {
        return new ObjectLinkedOpenHashSet<>(
                Lists.newArrayList(WILDCARD, ResourceKey.of(id.namespace(), "_"), ResourceKey.of("_", id.value()), id));
    }

    boolean remove(RestrictionRule rule);

    Stream<ResourceKey> predicates();

    Set<RestrictionRule> rule(ResourceKey predicate);

    /**
     * Register a predicate
     *
     * @param rule rule with predicate
     * @return registered predicate for rule
     */
    Set<ResourceKey> register(RestrictionRule rule);

    void clear();
}
