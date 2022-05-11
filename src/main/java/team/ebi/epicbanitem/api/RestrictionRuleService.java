package team.ebi.epicbanitem.api;

import com.google.common.collect.Sets;
import com.google.inject.ImplementedBy;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;
import team.ebi.epicbanitem.EBIEventContextKeys;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.rule.RestrictionRuleServiceImpl;

@ImplementedBy(RestrictionRuleServiceImpl.class)
public interface RestrictionRuleService {

  default Optional<UpdateOperation> restrict() {
    return restrict(Sponge.server().causeStackManager().currentContext());
  }

  default Optional<UpdateOperation> restrict(EventContext context) {
    return restrict(context, context.require(EBIEventContextKeys.RESTRICTED_OBJECT).toContainer());
  }

  default Optional<UpdateOperation> restrict(EventContext context, DataView view) {
    RestrictionRule rule = context.require(EBIEventContextKeys.RESTRICTION_RULE);
    ServerWorld world = context.require(EBIEventContextKeys.OBJECT_RESTRICT);
    RestrictionTrigger trigger = context.require(EBIEventContextKeys.RESTRICTION_TRIGGER);
    Optional<Subject> subject = context.get(EventContextKeys.SUBJECT);
    return restrict(rule, view, world, trigger, subject.orElse(null));
  }

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
    if (Objects.nonNull(subject) && shouldBypass(subject, rule, trigger)) return Optional.empty();
    Optional<QueryResult> queryResult = rule.queryExpression().query(view);
    return queryResult.flatMap(
        result -> Optional.ofNullable(rule.updateExpression()).map(it -> it.update(result, view)));
  }
}
