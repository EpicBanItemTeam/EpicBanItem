package team.ebi.epicbanitem.util;

import java.util.Optional;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;

public class EntityUtils {

  private EntityUtils() {}

  public static Optional<ItemStack> targetObject(Player player, boolean isBlock) {
    return Optional.of(isBlock)
        .filter(Boolean::booleanValue)
        .flatMap(ignored -> targetBlock(player).flatMap(BlockUtils::fromBlock))
        .or(() -> heldHand(player).flatMap(it -> equipped(player, it)));
  }

  public static Optional<LocatableBlock> targetLocation(Living living) {
    return RayTrace.block()
        .select(
            RayTrace.nonAir()
                .and(
                    block ->
                        (!block.blockState().get(Keys.IS_PASSABLE).orElse(true))
                            || (block
                                .blockState()
                                .get(Keys.MATTER_TYPE)
                                .map(matter -> matter.equals(MatterTypes.SOLID.get()))
                                .orElse(false))))
        .limit(5)
        .sourceEyePosition(living)
        .direction(living)
        .execute()
        .map(RayTraceResult::selectedObject);
  }

  public static Optional<BlockSnapshot> targetBlock(Living living) {
    return targetLocation(living).map(it -> it.serverLocation().createSnapshot());
  }

  public static Optional<EquipmentType> heldHand(@NotNull Equipable equipable) {
    return equipable
        .equipped(EquipmentTypes.MAIN_HAND.get())
        .filter(Predicate.not(ItemStack::isEmpty))
        .map(it -> EquipmentTypes.MAIN_HAND.get())
        .or(
            () ->
                equipable
                    .equipped(EquipmentTypes.OFF_HAND.get())
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .map(it -> EquipmentTypes.OFF_HAND.get()));
  }

  public static Optional<ItemStack> equipped(@NotNull Equipable equipable, EquipmentType type) {
    return equipable.equipped(type).filter(Predicate.not(ItemStack::isEmpty));
  }
}
