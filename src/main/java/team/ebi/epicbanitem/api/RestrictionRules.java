package team.ebi.epicbanitem.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;

public class RestrictionRules {
  private static final BiMap<ResourceKey, RestrictionRule> map = HashBiMap.create();

  /**
   * @see RestrictionRuleService#register(ResourceKey,RestrictionRule)
   */
  @Internal
  public static RestrictionRule add(ResourceKey key, RestrictionRule rule) {
    return map.put(key, rule);
  }

  public static BiMap<ResourceKey, RestrictionRule> all() {
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

  static {
    Sponge.dataManager().registerBuilder(RestrictionRule.class, new RestrictionRuleImpl.Builder());
  }
}
