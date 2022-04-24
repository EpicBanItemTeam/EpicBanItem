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

  void needCancel(boolean value);

  boolean defaultWorldState();

  boolean defaultTriggerState();

  void defaultWorldState(boolean state);

  void defaultTriggerState(boolean state);

  boolean worldState(UUID uuid);

  boolean worldState(@NotNull UUID uuid, boolean value);

  ImmutableMap<UUID, Boolean> worldStates();

  boolean triggerState(RestrictionTrigger trigger);

  boolean triggerState(@NotNull RestrictionTrigger trigger, boolean value);

  ImmutableMap<RestrictionTrigger, Boolean> triggersState();

  QueryExpression queryExpression();

  @Nullable
  UpdateExpression updateExpression();

  void queryExpression(QueryExpression expression);

  void updateExpression(@Nullable UpdateExpression expression);

  /**
   *
   * <li>"minecraft:*" will try to match rule on all minecraft objects
   * <li>"*:*" will try to match rule on all objects
   * <li>"minecraft:dirt" will only try to match rule when target is dirt
   *
   * @return The id filter for performance.
   */
  ResourceKey predicate();

  void predicate(ResourceKey key);

  /**
   * @return Translatable component with args:
   * 0: rule
   * 1: trigger
   * 2: origin object name
   * 3: final object name
   */
  @Contract(pure = true)
  TranslatableComponent updatedMessage();

  /**
   * @return Translatable component with args:
   * 0: rule
   * 1: trigger
   * 2: origin object name
   */
  @Contract(pure = true)
  TranslatableComponent canceledMessage();

  @Override
  @NotNull
  Component asComponent();
}
