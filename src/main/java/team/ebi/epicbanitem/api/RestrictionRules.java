package team.ebi.epicbanitem.api;

import com.google.common.collect.Maps;
import java.util.Map;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;

public class RestrictionRules {

  /** Load from configuration file */
  public static final Map<ResourceKey, RestrictionRule> DEFAULT_REGISTRIES = Maps.newHashMap();

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
