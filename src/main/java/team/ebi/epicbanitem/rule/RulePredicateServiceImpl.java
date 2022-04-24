package team.ebi.epicbanitem.rule;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RulePredicateService;

public class RulePredicateServiceImpl implements RulePredicateService {
  private final SetMultimap<ResourceKey, RestrictionRule> predicates = HashMultimap.create();

  @Inject
  public RulePredicateServiceImpl() {}

  @Override
  public ImmutableSet<ResourceKey> predicates() {
    return ImmutableSet.copyOf(predicates.keySet());
  }

  @Override
  public ImmutableSet<RestrictionRule> rule(ResourceKey predicate) {
    return ImmutableSet.copyOf(this.predicates.get(predicate));
  }

  @Override
  public ImmutableSet<ResourceKey> register(RestrictionRule rule) {
    ImmutableSet<ResourceKey> keys = predicates(rule.predicate());
    for (ResourceKey key : keys) predicates.put(key, rule);
    return keys;
  }
}
