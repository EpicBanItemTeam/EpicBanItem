package team.ebi.epicbanitem.rule;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleService;
import team.ebi.epicbanitem.api.RulePredicateService;

@Singleton
public class RestrictionRuleServiceImpl implements RestrictionRuleService {

  private final BiMap<ResourceKey, RestrictionRule> map = HashBiMap.create();

  @Inject
  private RulePredicateService predicateService;

  @Inject
  public RestrictionRuleServiceImpl() {
    Sponge.dataManager().registerBuilder(RestrictionRule.class, new RestrictionRuleImpl.Builder());
  }

  @Override
  public Optional<ResourceKey> register(ResourceKey key, RestrictionRule rule) {
    RestrictionRule putted = map.put(key, rule);
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
  public RestrictionRule remove(ResourceKey key) {
    return map.remove(key);
  }
}
