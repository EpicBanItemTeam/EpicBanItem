package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableMap;
import java.util.UUID;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;

public interface RestrictionRule extends DefaultedRegistryValue, ComponentLike {
  /**
   * @return The priority of current rule (ASC, lower first). <br>
   *     Default: 10
   */
  int priority();

  boolean defaultWorldState();

  boolean defaultTriggerState();

  void defaultWorldState(boolean state);

  void defaultTriggerState(boolean state);

  boolean worldState(UUID uuid);

  boolean worldState(@NotNull UUID uuid, boolean value);

  ImmutableMap<UUID, Boolean> worldStates();

  boolean triggerState(Trigger trigger);

  boolean triggerState(@NotNull Trigger trigger, boolean value);

  ImmutableMap<Trigger, Boolean> triggersState();

  QueryExpression queryExpression();

  UpdateExpression updateExpression();

  void queryExpression(QueryExpression expression);

  void updateExpression(UpdateExpression expression);

  /**
   *
   * <li>"minecraft:*" will try to match rule on all minecraft objects
   * <li>"*:*" will try to match rule on all objects
   * <li>"minecraft:dirt" will only try to match rule when target is dirt
   *
   * @return The id filter for performance.
   */
  ResourceKey predicate();
}
