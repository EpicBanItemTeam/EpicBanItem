package team.ebi.epicbanitem.api;

import com.google.inject.Singleton;
import java.util.Optional;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;

@Singleton
public class RestrictionRules {
  public static Optional<RegistryEntry<RestrictionRule>> register(
      ResourceKey key, RestrictionRule rule) {
    Sponge.server()
        .serviceProvider()
        .provide(RulePredicateService.class)
        .ifPresent(service -> service.register(rule));
    return registry().register(key, rule);
  }

  public static Registry<RestrictionRule> registry() {
    return EBIRegistries.RESTRICTION_RULE.get();
  }

  private static DefaultedRegistryReference<RestrictionRule> key(final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.RESTRICTION_RULE, location)
        .asDefaultedReference(Sponge::server);
  }

  static {
    Sponge.dataManager()
        .registerBuilder(RestrictionRuleImpl.class, new RestrictionRuleImpl.Builder());
  }
}
