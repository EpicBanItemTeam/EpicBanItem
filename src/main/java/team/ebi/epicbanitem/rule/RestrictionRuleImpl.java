package team.ebi.epicbanitem.rule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleQueries;
import team.ebi.epicbanitem.api.RestrictionRules;
import team.ebi.epicbanitem.api.RestrictionTrigger;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;

public class RestrictionRuleImpl implements RestrictionRule {

  private final int priority;
  private boolean defaultWorldState;
  private boolean defaultTriggerState;

  private final Map<UUID, Boolean> worldStates = Maps.newHashMap();

  private final Map<RestrictionTrigger, Boolean> triggerStates = Maps.newHashMap();
  private QueryExpression queryExpression;
  private @Nullable UpdateExpression updateExpression;
  private ResourceKey predicate;
  private boolean needCancel;

  public RestrictionRuleImpl(DataView data) {
    this.priority = data.getInt(RestrictionRuleQueries.PRIORITY).orElse(10);
    this.queryExpression =
        data.getSerializable(RestrictionRuleQueries.QUERY, RootQueryExpression.class)
            .orElseThrow(
                () ->
                    new InvalidDataException(
                        MessageFormat.format("Invalid query expression for rule {}", key())));
    this.updateExpression =
        data.getSerializable(RestrictionRuleQueries.QUERY, RootUpdateExpression.class).orElse(null);
    this.predicate =
        data.getResourceKey(RestrictionRuleQueries.PREDICATE).orElse(RulePredicateService.WILDCARD);
    this.needCancel = data.getBoolean(RestrictionRuleQueries.NEED_CANCEL).orElse(false);
  }

  @Override
  public @NotNull ResourceKey key() {
    return RestrictionRules.of(this)
        .orElseThrow(() -> new IllegalArgumentException("Rule have to registered to get key"));
  }

  @Override
  public int priority() {
    return this.priority;
  }

  @Override
  public boolean needCancel() {
    return this.needCancel;
  }

  @Override
  public void needCancel(boolean value) {
    this.needCancel = value;
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
  public boolean triggerState(RestrictionTrigger trigger) {
    return this.triggerStates.getOrDefault(trigger, defaultTriggerState);
  }

  @Override
  public boolean triggerState(@NotNull RestrictionTrigger trigger, boolean value) {
    return Boolean.TRUE.equals(this.triggerStates.put(trigger, value));
  }

  @Override
  public ImmutableMap<RestrictionTrigger, Boolean> triggersState() {
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
  public TranslatableComponent message() {
    return Component.translatable("rules." + key() + ".message");
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
        .set(RestrictionRuleQueries.PREDICATE, predicate)
        .set(RestrictionRuleQueries.NEED_CANCEL, needCancel);
    return container.set(Queries.CONTENT_VERSION, contentVersion());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RestrictionRuleImpl that = (RestrictionRuleImpl) o;

    return new EqualsBuilder()
        .append(priority, that.priority)
        .append(defaultWorldState, that.defaultWorldState)
        .append(defaultTriggerState, that.defaultTriggerState)
        .append(worldStates, that.worldStates)
        .append(triggerStates, that.triggerStates)
        .append(queryExpression, that.queryExpression)
        .append(updateExpression, that.updateExpression)
        .append(predicate, that.predicate)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(priority)
        .append(defaultWorldState)
        .append(defaultTriggerState)
        .append(worldStates)
        .append(triggerStates)
        .append(queryExpression)
        .append(updateExpression)
        .append(predicate)
        .toHashCode();
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
