/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.rule;

import java.util.Objects;
import java.util.Optional;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.EBIServices;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.rule.*;
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
    private final boolean onlyPlayer;

    public RestrictionRuleImpl(QueryExpression queryExpression) {
        this.priority = 10;
        this.queryExpression = queryExpression;
        this.updateExpression = null;
        this.predicate = RulePredicateService.WILDCARD;
        this.needCancel = false;
        this.worldStates = new WorldStates(true);
        this.triggerStates = new TriggerStates(true);
        this.onlyPlayer = true;
    }

    public RestrictionRuleImpl(
            int priority,
            WorldStates worldStates,
            TriggerStates triggerStates,
            QueryExpression queryExpression,
            @Nullable UpdateExpression updateExpression,
            ResourceKey predicate,
            boolean needCancel,
            boolean onlyPlayer) {
        this.priority = priority;
        this.worldStates = worldStates;
        this.triggerStates = triggerStates;
        this.queryExpression = queryExpression;
        this.updateExpression = updateExpression;
        this.predicate = predicate;
        this.needCancel = needCancel;
        this.onlyPlayer = onlyPlayer;
    }

    public RestrictionRuleImpl(DataView data) {
        DataView view = data.getView(RestrictionRuleQueries.RULE).orElseThrow();
        this.priority = view.getInt(RestrictionRuleQueries.PRIORITY).orElse(10);
        this.queryExpression = view.getSerializable(RestrictionRuleQueries.QUERY, RootQueryExpression.class)
                .orElseThrow(() -> new InvalidDataException("Invalid query expression for rule"));
        this.updateExpression = view.getSerializable(RestrictionRuleQueries.UPDATE, RootUpdateExpression.class)
                .orElse(null);
        this.predicate = view.getResourceKey(RestrictionRuleQueries.PREDICATE).orElse(RulePredicateService.WILDCARD);
        this.needCancel = view.getBoolean(RestrictionRuleQueries.NEED_CANCEL).orElse(false);
        this.onlyPlayer = view.getBoolean(RestrictionRuleQueries.ONLY_PLAYER).orElse(true);
        // TODO Need config
        this.worldStates = view.getSerializable(RestrictionRuleQueries.WORLD, WorldStates.class)
                .orElse(new WorldStates(true));
        this.triggerStates = view.getSerializable(RestrictionRuleQueries.TRIGGER, TriggerStates.class)
                .orElse(new TriggerStates(true));
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
    public RestrictionRule priority(int value) {
        return new RestrictionRuleImpl(
                value,
                worldStates,
                triggerStates,
                queryExpression,
                updateExpression,
                predicate,
                needCancel,
                onlyPlayer);
    }

    @Override
    public boolean needCancel() {
        return this.needCancel;
    }

    @Override
    public RestrictionRule needCancel(boolean value) {
        return new RestrictionRuleImpl(
                priority, worldStates, triggerStates, queryExpression, updateExpression, predicate, value, onlyPlayer);
    }

    @Override
    public boolean onlyPlayer() {
        return onlyPlayer;
    }

    @Override
    public RestrictionRule onlyPlayer(boolean value) {
        return new RestrictionRuleImpl(
                priority, worldStates, triggerStates, queryExpression, updateExpression, predicate, needCancel, value);
    }

    @Override
    public WorldStates worldStates() {
        return worldStates;
    }

    @Override
    public RestrictionRule worldStates(WorldStates states) {
        return new RestrictionRuleImpl(
                priority, states, triggerStates, queryExpression, updateExpression, predicate, needCancel, onlyPlayer);
    }

    public TriggerStates triggerStates() {
        return triggerStates;
    }

    @Override
    public RestrictionRule triggerStates(TriggerStates states) {
        return new RestrictionRuleImpl(
                priority, worldStates, states, queryExpression, updateExpression, predicate, needCancel, onlyPlayer);
    }

    @Override
    public QueryExpression queryExpression() {
        return this.queryExpression;
    }

    @Override
    public RestrictionRule queryExpression(QueryExpression value) {
        return new RestrictionRuleImpl(
                priority, worldStates, triggerStates, value, updateExpression, predicate, needCancel, onlyPlayer);
    }

    @Override
    public Optional<UpdateExpression> updateExpression() {
        return Optional.ofNullable(this.updateExpression);
    }

    @Override
    public @Nullable RestrictionRule updateExpression(UpdateExpression value) {

        return new RestrictionRuleImpl(
                priority, worldStates, triggerStates, queryExpression, value, predicate, needCancel, onlyPlayer);
    }

    @Override
    public ResourceKey predicate() {
        return this.predicate;
    }

    @Override
    public RestrictionRule predicate(ResourceKey value) {
        return new RestrictionRuleImpl(
                priority, worldStates, triggerStates, queryExpression, updateExpression, value, needCancel, onlyPlayer);
    }

    private String messageKey(String path) {
        return "epicbanitem.rules." + path;
    }

    @Override
    public Optional<TranslatableComponent> updatedMessage() {
        final var key = messageKey(key() + ".updated");
        if (!EpicBanItem.translations.contains(key)) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable(key));
    }

    @Override
    public Optional<TranslatableComponent> cancelledMessage() {
        final var key = messageKey(key() + ".canceled");
        if (!EpicBanItem.translations.contains(key)) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable(key));
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
                .set(RestrictionRuleQueries.WORLD, worldStates)
                .set(RestrictionRuleQueries.TRIGGER, triggerStates)
                .set(RestrictionRuleQueries.PREDICATE, predicate)
                .set(RestrictionRuleQueries.NEED_CANCEL, needCancel)
                .set(RestrictionRuleQueries.ONLY_PLAYER, onlyPlayer);
        return container.set(Queries.CONTENT_VERSION, contentVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestrictionRuleImpl that = (RestrictionRuleImpl) o;
        return priority == that.priority
                && needCancel == that.needCancel
                && worldStates.equals(that.worldStates)
                && triggerStates.equals(that.triggerStates)
                && queryExpression.equals(that.queryExpression)
                && Objects.equals(updateExpression, that.updateExpression)
                && predicate.equals(that.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                priority, worldStates, triggerStates, queryExpression, updateExpression, predicate, needCancel);
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
