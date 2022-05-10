package team.ebi.epicbanitem.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Tuple;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;

public class RestrictionRules {
  private static final BiMap<ResourceKey, RestrictionRule> map = HashBiMap.create();

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

  public static Stream<ResourceKey> keyStream() {
    return map.keySet().stream();
  }

  public static RestrictionRule remove(ResourceKey key) {
    return map.remove(key);
  }

  public static RestrictionRule get(ResourceKey key) {
    return map.get(key);
  }

  public static RestrictionRule add(RestrictionRule rule) {
    return map.put(rule.key(), rule);
  }

  public static Optional<ResourceKey> of(RestrictionRule rule) {
    return Optional.ofNullable(map.inverse().get(rule));
  }

  static {
    Sponge.dataManager()
        .registerBuilder(RestrictionRuleImpl.class, new RestrictionRuleImpl.Builder());
  }
}
