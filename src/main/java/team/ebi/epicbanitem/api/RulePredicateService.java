package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.spongepowered.api.ResourceKey;

public interface RulePredicateService {
  ResourceKey WILDCARD = ResourceKey.of("*", "*");

  default ImmutableSet<RestrictionRule> rules(ResourceKey id) {
    return predicates(id).stream()
        .map(this::get)
        .flatMap(Collection::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * @param key Object key
   * @return All possible predicates
   */
  default ImmutableSet<ResourceKey> predicates(ResourceKey key) {
    return ImmutableSet.<ResourceKey>builder()
        .add(WILDCARD)
        .add(key)
        .add(ResourceKey.of(key.namespace(), "*"))
        .add(ResourceKey.of("*", key.value()))
        .build();
  }

  ImmutableSet<ResourceKey> predicates();

  ImmutableSet<RestrictionRule> get(ResourceKey predicate);

  /**
   * Register a predicate
   * @param rule rule with predicate
   * @return registered predicate for rule
   */
  ImmutableSet<ResourceKey> register(RestrictionRule rule);
}
