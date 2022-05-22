package team.ebi.epicbanitem.api.rule;

import com.google.common.collect.ImmutableBiMap;
import com.google.inject.ImplementedBy;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.spongepowered.api.ResourceKey;
import team.ebi.epicbanitem.rule.RestrictionRuleServiceImpl;

@ImplementedBy(RestrictionRuleServiceImpl.class)
public interface RestrictionRuleService {

  Optional<ResourceKey> register(ResourceKey key, RestrictionRule rule);

  default Optional<ResourceKey> of(RestrictionRule rule) {
    return Optional.ofNullable(all().inverse().get(rule));
  }

  default Optional<RestrictionRule> of(ResourceKey key) {
    return Optional.ofNullable(all().get(key));
  }

  @Contract(pure = true)
  ImmutableBiMap<ResourceKey, RestrictionRule> all();

  default Stream<ResourceKey> keys() {
    return all().keySet().stream();
  }

  void clear();

  RestrictionRule remove(ResourceKey key);

  void save();
}
