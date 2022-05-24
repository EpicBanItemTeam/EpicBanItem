/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.rule;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.*;
import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.EBIServices;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RestrictionRuleQueries;
import team.ebi.epicbanitem.api.rule.RulePredicateService;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;

public class RestrictionRuleImpl implements RestrictionRule {

    private final int priority;

    private final WorldStates worldStates;

    private final TriggerStates triggerStates;
    private final QueryExpression queryExpression;
    private final @Nullable UpdateExpression updateExpression;
    private final ResourceKey predicate;
    private final boolean needCancel;

    public RestrictionRuleImpl(QueryExpression queryExpression) {
        this.priority = 10;
        this.queryExpression = queryExpression;
        this.updateExpression = null;
        this.predicate = RulePredicateService.WILDCARD;
        this.needCancel = false;
        this.worldStates = new WorldStates(true);
        this.triggerStates = new TriggerStates(true);
    }

    public RestrictionRuleImpl(
            int priority,
            QueryExpression queryExpression,
            @Nullable UpdateExpression updateExpression,
            ResourceKey predicate,
            boolean needCancel) {
        this.priority = priority;
        this.queryExpression = queryExpression;
        this.updateExpression = updateExpression;
        this.predicate = predicate;
        this.needCancel = needCancel;
        this.worldStates = new WorldStates(true);
        this.triggerStates = new TriggerStates(true);
    }

    public RestrictionRuleImpl(DataView data) {
        DataView view = data.getView(RestrictionRuleQueries.RULE).orElseThrow();
        this.priority = view.getInt(RestrictionRuleQueries.PRIORITY).orElse(10);
        this.queryExpression = view.getSerializable(RestrictionRuleQueries.QUERY, RootQueryExpression.class)
                .orElseThrow(() -> new InvalidDataException("Invalid query expression for rule"));
        this.updateExpression = view.getSerializable(RestrictionRuleQueries.QUERY, RootUpdateExpression.class)
                .orElse(null);
        this.predicate = view.getResourceKey(RestrictionRuleQueries.PREDICATE).orElse(RulePredicateService.WILDCARD);
        this.needCancel = view.getBoolean(RestrictionRuleQueries.NEED_CANCEL).orElse(false);
        // TODO Need config
        this.worldStates = new WorldStates(
                view.getBoolean(RestrictionRuleQueries.WORLD.then(RestrictionRuleQueries.DEFAULT))
                        .orElse(false),
                view.getMap(RestrictionRuleQueries.WORLD.then(RestrictionRuleQueries.STATES))
                        .map(it -> it.entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> ResourceKey.resolve(
                                                entry.getKey().toString()),
                                        entry -> Tristate.valueOf(
                                                entry.getValue().toString().toUpperCase()))))
                        .orElse(Maps.newHashMap()));
        this.triggerStates = new TriggerStates(
                view.getBoolean(RestrictionRuleQueries.TRIGGER.then(RestrictionRuleQueries.DEFAULT))
                        .orElse(false),
                view.getMap(RestrictionRuleQueries.WORLD.then(RestrictionRuleQueries.STATES))
                        .map(it -> it.entrySet().stream()
                                .collect(Collectors.toMap(
                                        entry -> ResourceKey.resolve(
                                                entry.getKey().toString()),
                                        entry -> Tristate.valueOf(
                                                entry.getValue().toString().toUpperCase()))))
                        .orElse(Maps.newHashMap()));
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
    public boolean worldState(ResourceKey key) {
        return this.worldStates.getOrDefault(key);
    }

    @Override
    public boolean triggerState(ResourceKey key) {
        return this.triggerStates.getOrDefault(key);
    }

    @Override
    public WorldStates worldStates() {
        return worldStates;
    }

    public TriggerStates triggerStates() {
        return triggerStates;
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
        final var key = messageKey(key() + ".updated");
        if (!EpicBanItem.translations.contains(key)) {
            return Component.translatable("epicbanitem.rule.updated");
        }
        return Component.translatable(key);
    }

    @Override
    public TranslatableComponent canceledMessage() {
        final var key = messageKey(key() + ".canceled");
        if (!EpicBanItem.translations.contains(key)) {
            return Component.translatable("epicbanitem.rule.canceled");
        }
        return Component.translatable(key);
    }

    @Override
    public @NotNull Component asComponent() {
        final var resourceKey = key();
        final var key = messageKey(resourceKey.asString());
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
        var container = DataContainer.createNew();
        var ruleView = container.createView(RestrictionRuleQueries.RULE);
        if (Objects.nonNull(updateExpression)) {
            ruleView.set(RestrictionRuleQueries.UPDATE, updateExpression);
        }
        ruleView.set(RestrictionRuleQueries.PRIORITY, priority)
                .set(RestrictionRuleQueries.QUERY, queryExpression)
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

        var that = (RestrictionRuleImpl) o;

        return new EqualsBuilder()
                .append(priority, that.priority)
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
        protected Optional<RestrictionRule> buildContent(DataView container) throws InvalidDataException {
            return Optional.of(new RestrictionRuleImpl(container));
        }
    }
}
