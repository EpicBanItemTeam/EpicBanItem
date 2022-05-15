package team.ebi.epicbanitem.rule;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Singleton;
import java.util.Set;
import java.util.stream.Stream;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RulePredicateService;

@Singleton
public class RulePredicateServiceImpl implements RulePredicateService {

  private final SetMultimap<ResourceKey, RestrictionRule> predicates = HashMultimap.create();

  @Override
  public boolean remove(RestrictionRule rule) {
    return predicates(rule.predicate()).stream()
        .reduce(
            false,
            (prev, curr) -> prev || predicates.get(curr).remove(rule),
            (prev, curr) -> prev || curr
        );
  }

  @Override
  public Stream<ResourceKey> predicates() {
    return predicates.keySet().stream();
  }

  @Override
  public Set<RestrictionRule> rule(ResourceKey predicate) {
    return this.predicates.get(predicate);
  }

  @Override
  public Set<ResourceKey> register(RestrictionRule rule) {
    Set<ResourceKey> keys = predicates(rule.predicate());
    for (ResourceKey key : keys) {
      predicates.put(key, rule);
    }
    return keys;
  }
}
