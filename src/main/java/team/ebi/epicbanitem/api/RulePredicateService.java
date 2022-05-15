package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.ImplementedBy;
import java.util.Collection;
import java.util.Comparator;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.rule.RulePredicateServiceImpl;

@ImplementedBy(RulePredicateServiceImpl.class)
public interface RulePredicateService {
  ResourceKey WILDCARD = ResourceKey.of("_", "_");

  /**
   * @param id {@link ResourceKey} of object
   * @return Rules that match the predicates
   */
  @SuppressWarnings("UnstableApiUsage")
  default ImmutableSet<RestrictionRule> rules(ResourceKey id) {
    return predicates(id).stream()
        .map(this::rule)
        .flatMap(Collection::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @SuppressWarnings("UnstableApiUsage")
  default ImmutableSortedSet<RestrictionRule> rulesWithPriority(ResourceKey id) {
    return rules(id).stream()
        .collect(
            ImmutableSortedSet.toImmutableSortedSet(
                Comparator.comparingInt(RestrictionRule::priority)));
  }

  /**
   * @param key Object key
   * @return All possible predicates
   */
  default ImmutableSet<ResourceKey> predicates(ResourceKey key) {
    return ImmutableSet.<ResourceKey>builder()
        .add(WILDCARD)
        .add(key)
        .add(ResourceKey.of(key.namespace(), "_"))
        .add(ResourceKey.of("_", key.value()))
        .build();
  }

  ImmutableSet<ResourceKey> predicates();

  ImmutableSet<RestrictionRule> rule(ResourceKey predicate);

  /**
   * Register a predicate
   *
   * @param rule rule with predicate
   * @return registered predicate for rule
   */
  ImmutableSet<ResourceKey> register(RestrictionRule rule);
}
