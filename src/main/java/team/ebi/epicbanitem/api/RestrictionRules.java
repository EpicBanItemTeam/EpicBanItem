package team.ebi.epicbanitem.api;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Tuple;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;

public class RestrictionRules {
  private static final Map<ResourceKey, RestrictionRule> map = Maps.newHashMap();

  public static Optional<Tuple<ResourceKey, RestrictionRule>> register(
      ResourceKey key, RestrictionRule rule) {
    Sponge.server()
        .serviceProvider()
        .provide(RulePredicateService.class)
        .orElseThrow(() -> new IllegalStateException("RulePredicateService have to be provided"))
        .register(rule);
    RestrictionRule putted = map.put(key, rule);
    if (putted == null) return Optional.empty();
    else return Optional.of(Tuple.of(key, putted));
  }

  public static Map<ResourceKey, RestrictionRule> all() {
    return map;
  }

  public static RestrictionRule remove(ResourceKey key) {
    return map.remove(key);
  }

  public static Optional<ResourceKey> of(RestrictionRule rule) {
    return map.entrySet().stream()
        .filter(it -> it.getValue().equals(rule))
        .map(Entry::getKey)
        .findFirst();
  }

  static {
    Sponge.dataManager()
        .registerBuilder(RestrictionRuleImpl.class, new RestrictionRuleImpl.Builder());
  }
}
