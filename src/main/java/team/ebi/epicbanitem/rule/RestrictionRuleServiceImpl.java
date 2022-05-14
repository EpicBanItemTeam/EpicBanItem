package team.ebi.epicbanitem.rule;

import com.google.inject.Inject;
import java.util.Optional;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleService;
import team.ebi.epicbanitem.api.RestrictionRules;
import team.ebi.epicbanitem.api.RulePredicateService;

public class RestrictionRuleServiceImpl implements RestrictionRuleService {

  @Inject private RulePredicateService predicateService;

  @Override
  public Optional<ResourceKey> register(ResourceKey key, RestrictionRule rule) {
    predicateService.register(rule);
    RestrictionRule putted = RestrictionRules.add(key, rule);
    if (putted == null) return Optional.empty();
    else return Optional.of(key);
  }
}
