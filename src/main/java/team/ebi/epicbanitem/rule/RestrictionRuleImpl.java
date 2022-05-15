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
import team.ebi.epicbanitem.EBIServices;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleQueries;
import team.ebi.epicbanitem.api.RestrictionTrigger;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;

public class RestrictionRuleImpl implements RestrictionRule {

  private final int priority;
  private final boolean defaultWorldState;
  private final boolean defaultTriggerState;

  private final Map<UUID, Boolean> worldStates = Maps.newHashMap();

  private final Map<RestrictionTrigger, Boolean> triggerStates = Maps.newHashMap();
  private final QueryExpression queryExpression;
  private final @Nullable UpdateExpression updateExpression;
  private final ResourceKey predicate;
  private final boolean needCancel;

  public RestrictionRuleImpl(QueryExpression queryExpression) {
    this(10, false, false, queryExpression, null, RulePredicateService.WILDCARD, false);
  }

  public RestrictionRuleImpl(
      int priority,
      boolean defaultWorldState,
      boolean defaultTriggerState,
      QueryExpression queryExpression,
      @Nullable UpdateExpression updateExpression,
      ResourceKey predicate,
      boolean needCancel) {
    this.priority = priority;
    this.defaultWorldState = defaultWorldState;
    this.defaultTriggerState = defaultTriggerState;
    this.queryExpression = queryExpression;
    this.updateExpression = updateExpression;
    this.predicate = predicate;
    this.needCancel = needCancel;
  }

  public RestrictionRuleImpl(DataView data) {
    this.priority = data.getInt(RestrictionRuleQueries.PRIORITY).orElse(10);
    this.queryExpression =
        data.getSerializable(RestrictionRuleQueries.QUERY, RootQueryExpression.class)
            .orElseThrow(
                () ->
                    new InvalidDataException(
                        MessageFormat.format("Invalid query expression for rule {0}", key())));
    this.updateExpression =
        data.getSerializable(RestrictionRuleQueries.QUERY, RootUpdateExpression.class).orElse(null);
    this.predicate =
        data.getResourceKey(RestrictionRuleQueries.PREDICATE).orElse(RulePredicateService.WILDCARD);
    this.needCancel = data.getBoolean(RestrictionRuleQueries.NEED_CANCEL).orElse(false);
    // TODO Need config
    this.defaultWorldState =
        data.getBoolean(RestrictionRuleQueries.DEFAULT_WORLD_STATE).orElse(false);
    this.defaultTriggerState =
        data.getBoolean(RestrictionRuleQueries.DEFAULT_TRIGGER_STATE).orElse(false);
  }

  @Override
  public @NotNull ResourceKey key() {
    return EBIServices.ruleService
        .of(this)
        .orElseThrow(() -> new IllegalArgumentException("Rule has to registered to get key"));
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
  public boolean defaultWorldState() {
    return this.defaultWorldState;
  }

  @Override
  public boolean defaultTriggerState() {
    return this.defaultTriggerState;
  }

  @Override
  public boolean worldState(UUID uuid) {
    return this.worldStates.getOrDefault(uuid, defaultWorldState);
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
  public ResourceKey predicate() {
    return this.predicate;
  }

  private String messageKey(String path) {
    return "epicbanitem.rules." + path;
  }

  @Override
  public TranslatableComponent updatedMessage() {
    String key = messageKey(key() + ".updated");
    if (!EpicBanItem.translations.contains(key)) {
      return Component.translatable("epicbanitem.rules.updated");
    }
    return Component.translatable(key);
  }

  @Override
  public TranslatableComponent canceledMessage() {
    String key = messageKey(key() + ".canceled");
    if (!EpicBanItem.translations.contains(key)) {
      return Component.translatable("epicbanitem.rules.canceled");
    }
    return Component.translatable(key);
  }

  @Override
  public @NotNull Component asComponent() {
    ResourceKey resourceKey = key();
    String key = messageKey(resourceKey.asString());
    if (!EpicBanItem.translations.contains(key)) {
      return Component.text(resourceKey.asString());
    }
    return Component.translatable(key);
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

  public static final class Builder extends AbstractDataBuilder<RestrictionRule> {

    public Builder() {
      super(RestrictionRule.class, 0);
    }

    @Override
    protected Optional<RestrictionRule> buildContent(DataView container)
        throws InvalidDataException {
      return Optional.of(new RestrictionRuleImpl(container));
    }
  }
}
