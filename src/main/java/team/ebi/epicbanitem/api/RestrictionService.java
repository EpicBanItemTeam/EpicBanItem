package team.ebi.epicbanitem.api;

import com.google.common.collect.Sets;
import com.google.inject.ImplementedBy;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.rule.RestrictionServiceImpl;

@ImplementedBy(RestrictionServiceImpl.class)
public interface RestrictionService {
  default boolean shouldBypass(Subject subject, RestrictionRule rule, RestrictionTrigger trigger) {
    return subject.hasPermission(
        EpicBanItem.permission("bypass." + rule),
        Sets.newHashSet(new Context(RestrictionTrigger.CONTEXT_KEY, trigger.key().asString())));
  }

  default Optional<UpdateOperation> restrict(
      RestrictionRule rule,
      DataView view,
      ServerWorld world,
      RestrictionTrigger trigger,
      @Nullable Subject subject) {
    if (!rule.triggerState(trigger)) return Optional.empty();
    if (!rule.worldState(world.uniqueId())) return Optional.empty();
    if (Objects.nonNull(subject) && shouldBypass(subject, rule, trigger)) return Optional.empty();
    return rule.queryExpression()
        .query(view)
        .flatMap(
            result ->
                Optional.ofNullable(rule.updateExpression()).map(it -> it.update(result, view)));
  }
}
