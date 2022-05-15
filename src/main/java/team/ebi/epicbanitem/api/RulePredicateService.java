package team.ebi.epicbanitem.api;

import com.google.inject.ImplementedBy;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
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
   * @param id Object id
   * @return All possible predicates
   */
  default Set<ResourceKey> predicates(ResourceKey id) {
    return Set.copyOf(List.of(
        WILDCARD,
        id,
        ResourceKey.of(id.namespace(), "_"),
        ResourceKey.of("_", id.value())));
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
}
