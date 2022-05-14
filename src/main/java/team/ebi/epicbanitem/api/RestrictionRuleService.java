package team.ebi.epicbanitem.api;

import com.google.inject.ImplementedBy;
import com.google.inject.Singleton;
import java.util.Optional;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.rule.RestrictionRuleServiceImpl;

@Singleton
@ImplementedBy(RestrictionRuleServiceImpl.class)
public interface RestrictionRuleService {
  Optional<ResourceKey> register(ResourceKey key, RestrictionRule rule);

  default Optional<ResourceKey> of(RestrictionRule rule) {
    return Optional.ofNullable(RestrictionRules.all().inverse().get(rule));
  }
}
