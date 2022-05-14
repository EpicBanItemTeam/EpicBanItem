package team.ebi.epicbanitem.trigger;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.AnimateHandEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerWorld;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.AbstractRestrictionTrigger;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class UseRestrictionTrigger extends AbstractRestrictionTrigger {
  @Inject private RulePredicateService predicateService;
  @Inject private RestrictionService restrictionService;

  public UseRestrictionTrigger() {
    super(EpicBanItem.key("use"));
  }

  @Listener
  public void onAnimateHand(
      AnimateHandEvent event,
      @Getter("handType") HandType hand,
      @Last ServerPlayer user,
      @Last ServerWorld world) {
    EquipmentType equipment =
        HandTypes.MAIN_HAND.get().equals(hand)
            ? EquipmentTypes.MAIN_HAND.get()
            : EquipmentTypes.OFF_HAND.get();
    Optional<Slot> slot = user.equipment().slot(equipment);
    if (slot.isEmpty()) return;
    ItemStack itemStack = slot.get().peek();
    ItemStackSnapshot item = itemStack.createSnapshot();
    ImmutableSortedSet<RestrictionRule> rules =
        predicateService.rulesWithPriority(item.type().key(RegistryTypes.ITEM_TYPE));
    List<Component> components = Lists.newArrayList();
    for (RestrictionRule rule : rules) {
      if (rule.needCancel()) {
        event.setCancelled(true);
        TranslatableComponent component = rule.canceledMessage();
        components.add(
            GlobalTranslator.render(component.args(rule, this, itemStack), user.locale()));
      }
      Optional<UpdateOperation> operation =
          restrictionService.restrict(rule, item.toContainer(), world, this, user);
      if (operation.isEmpty()) break;
      Optional<ItemStackSnapshot> result =
          Sponge.dataManager()
              .deserialize(ItemStackSnapshot.class, operation.get().process(item.toContainer()));
      if (result.isEmpty()) break;
      slot.get().offer(result.get().createStack());
      TranslatableComponent component = rule.updatedMessage();
      components.add(
          GlobalTranslator.render(
              component.args(rule, this, itemStack, result.get().createStack()), user.locale()));
    }
    for (Component component : components) user.sendMessage(component);
  }
}
