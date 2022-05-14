package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableMap;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.data.persistence.DataSerializable;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;

public interface RestrictionRule extends ResourceKeyed, ComponentLike, DataSerializable {
  /**
   * @return The priority of current rule (ASC, lower first). <br>
   *     Default: 10
   */
  int priority();

  boolean needCancel();

  boolean defaultWorldState();

  boolean defaultTriggerState();

  boolean worldState(UUID uuid);

  ImmutableMap<UUID, Boolean> worldStates();

  boolean triggerState(RestrictionTrigger trigger);

  ImmutableMap<RestrictionTrigger, Boolean> triggersState();

  QueryExpression queryExpression();

  @Nullable
  UpdateExpression updateExpression();

  /**
   *
   * <li>"minecraft:*" will try to match rule on all minecraft objects
   * <li>"*:*" will try to match rule on all objects
   * <li>"minecraft:dirt" will only try to match rule when target is dirt
   *
   * @return The id filter for performance.
   */
  ResourceKey predicate();

  /**
   * @return Translatable component with args:
   *     <li>0: rule
   *     <li>1: trigger
   *     <li>2: origin object name
   *     <li>3: final object name
   */
  @Contract(pure = true)
  TranslatableComponent updatedMessage();

  /**
   * @return Translatable component with args:
   *     <li>0: rule
   *     <li>1: trigger
   *     <li>2: origin object name
   */
  @Contract(pure = true)
  TranslatableComponent canceledMessage();

  @Override
  @NotNull Component asComponent();
}
