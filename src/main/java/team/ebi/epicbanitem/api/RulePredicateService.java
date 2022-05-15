package team.ebi.epicbanitem.api;

import com.google.inject.ImplementedBy;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.rule.RulePredicateServiceImpl;

@ImplementedBy(RulePredicateServiceImpl.class)
public interface RulePredicateService {

  ResourceKey WILDCARD = ResourceKey.of("_", "_");

  /**
   * @param id {@link ResourceKey} of object
   * @return Rules that match the predicates
   */
  default List<RestrictionRule> rules(ResourceKey id) {
    return predicates(id).stream()
        .map(this::rule)
        .flatMap(Collection::stream)
        .toList();
  }

  default List<RestrictionRule> rulesWithPriority(ResourceKey id) {
    return rules(id).stream()
        .sorted(Comparator.comparingInt(RestrictionRule::priority))
        .toList();
  }

  /**
   * @param key Object key
   * @return All possible predicates
   */
  default Set<ResourceKey> predicates(ResourceKey key) {
    return Set.of(
        WILDCARD,
        key,
        ResourceKey.of(key.namespace(), "_"),
        ResourceKey.of("_", key.value()));
  }

  Set<ResourceKey> predicates();

  Set<RestrictionRule> rule(ResourceKey predicate);

  /**
   * Register a predicate
   *
   * @param rule rule with predicate
   * @return registered predicate for rule
   */
  Set<ResourceKey> register(RestrictionRule rule);
}
