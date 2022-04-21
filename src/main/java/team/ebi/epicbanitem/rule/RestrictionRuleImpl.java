package team.ebi.epicbanitem.rule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleQueries;
import team.ebi.epicbanitem.api.Trigger;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;

public class RestrictionRuleImpl implements RestrictionRule {

  private final int priority;
  private boolean defaultWorldState;
  private boolean defaultTriggerState;

  private final Map<UUID, Boolean> worldStates = Maps.newHashMap();

  private final Map<Trigger, Boolean> triggerStates = Maps.newHashMap();
  private QueryExpression queryExpression;
  private UpdateExpression updateExpression;
  private ResourceKey predicate;

  public RestrictionRuleImpl(DataView data) {
    this.priority = data.getInt(RestrictionRuleQueries.PRIORITY).orElse(10);
    this.queryExpression =
        data.getSerializable(RestrictionRuleQueries.QUERY, RootQueryExpression.class)
            .orElseThrow(
                () ->
                    new InvalidDataException(
                        MessageFormat.format("Invalid query expression for rule {}", key())));
    this.updateExpression =
        data.getSerializable(RestrictionRuleQueries.QUERY, RootUpdateExpression.class)
            .orElseThrow(
                () ->
                    new InvalidDataException(
                        MessageFormat.format("Invalid update expression for rule {}", key())));
    this.predicate =
        data.getResourceKey(RestrictionRuleQueries.PREDICATE).orElse(ResourceKey.of("*", "*"));
  }

  public Key key() {
    return key(EBIRegistries.RESTRICTION_RULE);
  }

  @Override
  public int priority() {
    return this.priority;
  }

  @Override
  public boolean defaultWorldState() {
    return this.defaultWorldState;
  }

  @Override
  public boolean defaultTriggerState() {
    return this.defaultTriggerState;
  }

  @Override
  public void defaultWorldState(boolean state) {
    this.defaultWorldState = state;
  }

  @Override
  public void defaultTriggerState(boolean state) {
    this.defaultTriggerState = state;
  }

  @Override
  public boolean worldState(UUID uuid) {
    return this.worldStates.getOrDefault(uuid, defaultWorldState);
  }

  @Override
  public boolean worldState(@NotNull UUID uuid, boolean value) {
    return Boolean.TRUE.equals(this.worldStates.put(uuid, value));
  }

  @Override
  public ImmutableMap<UUID, Boolean> worldStates() {
    return ImmutableMap.copyOf(this.worldStates);
  }

  @Override
  public boolean triggerState(Trigger trigger) {
    return this.triggerStates.getOrDefault(trigger, defaultTriggerState);
  }

  @Override
  public boolean triggerState(@NotNull Trigger trigger, boolean value) {
    return Boolean.TRUE.equals(this.triggerStates.put(trigger, value));
  }

  @Override
  public ImmutableMap<Trigger, Boolean> triggersState() {
    return ImmutableMap.copyOf(this.triggerStates);
  }

  @Override
  public QueryExpression queryExpression() {
    return this.queryExpression;
  }

  @Override
  public UpdateExpression updateExpression() {
    return this.updateExpression;
  }

  @Override
  public void queryExpression(QueryExpression expression) {
    this.queryExpression = expression;
  }

  @Override
  public void updateExpression(UpdateExpression expression) {
    this.updateExpression = expression;
  }

  @Override
  public ResourceKey predicate() {
    return this.predicate;
  }

  @Override
  public void predicate(ResourceKey key) {
    this.predicate = key;
  }

  @Override
  public @NotNull Component asComponent() {
    return Component.translatable("rules." + key());
  }

  @Override
  public int contentVersion() {
    return 0;
  }

  @Override
  public DataContainer toContainer() {
    DataContainer container = DataContainer.createNew();
    container
        .createView(RestrictionRuleQueries.RULE)
        .set(RestrictionRuleQueries.PRIORITY, priority)
        .set(RestrictionRuleQueries.QUERY, queryExpression)
        .set(RestrictionRuleQueries.UPDATE, updateExpression)
        .set(RestrictionRuleQueries.PREDICATE, predicate);
    return container.set(Queries.CONTENT_VERSION, contentVersion());
  }

  public static final class Builder extends AbstractDataBuilder<RestrictionRuleImpl> {

    public Builder() {
      super(RestrictionRuleImpl.class, 0);
    }

    @Override
    protected Optional<RestrictionRuleImpl> buildContent(DataView container)
        throws InvalidDataException {
      return Optional.of(new RestrictionRuleImpl(container));
    }
  }
}
