package team.ebi.epicbanitem.trigger;

import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.AnimateHandEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.world.server.ServerWorld;
import team.ebi.epicbanitem.EBIEventContextKeys;
import team.ebi.epicbanitem.api.AbstractRestrictionTrigger;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleService;
import team.ebi.epicbanitem.api.RestrictionTriggers;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UseRestrictionTrigger extends AbstractRestrictionTrigger {

  public UseRestrictionTrigger() {
    super(RestrictionTriggers.USE);
  }

  @Listener
  public void onAnimateHand(
      AnimateHandEvent event,
      @Getter("handType") HandType hand,
      @Last ServerPlayer user,
      @Last ServerWorld world) {
    CauseStackManager causeStackManager = Sponge.server().causeStackManager();
    ServiceProvider serviceProvider = Sponge.server().serviceProvider();
    RestrictionRuleService ruleService =
        serviceProvider.provide(RestrictionRuleService.class).get();
    RulePredicateService predicateService =
        serviceProvider.provide(RulePredicateService.class).get();
    EquipmentType equipment =
        HandTypes.MAIN_HAND.get().equals(hand)
            ? EquipmentTypes.MAIN_HAND.get()
            : EquipmentTypes.OFF_HAND.get();
    Optional<Slot> slot = user.equipment().slot(equipment);
    if (!slot.isPresent()) return;
    ItemStackSnapshot item = slot.get().peek().createSnapshot();
    Optional<RestrictionRule> rule =
        predicateService.ruleWithPriority(item.type().key(RegistryTypes.ITEM_TYPE));
    if (!rule.isPresent()) return;
    if (rule.get().needCancel()) event.setCancelled(true);
    causeStackManager
        .addContext(EBIEventContextKeys.RESTRICTION_RULE, rule.get())
        .addContext(EBIEventContextKeys.OBJECT_RESTRICT, world)
        .addContext(EBIEventContextKeys.RESTRICTION_TRIGGER, this)
        .addContext(EBIEventContextKeys.RESTRICTED_OBJECT, item)
        .addContext(EventContextKeys.SUBJECT, user);
    Optional<UpdateOperation> operation = ruleService.restrict();
    if (!operation.isPresent()) return;
    Optional<ItemStackSnapshot> result =
        Sponge.dataManager()
            .deserialize(ItemStackSnapshot.class, operation.get().process(item.toContainer()));
    if (!result.isPresent()) return;
    slot.get().offer(result.get().createStack());
  }

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("trigger." + key() + ".name");
  }

  @Override
  public Component description() {
    return Component.translatable("trigger." + key() + ".description");
  }
}
