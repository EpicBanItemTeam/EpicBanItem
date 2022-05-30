/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RulePredicateService;

public abstract class SingleTargetRestrictionTrigger extends AbstractRestrictionTrigger {

    @Inject
    private RulePredicateService predicateService;

    @Inject
    private RestrictionService restrictionService;

    protected SingleTargetRestrictionTrigger(ResourceKey key) {
        super(key);
    }

    protected Optional<ItemStackSnapshot> processWithMessage(Event event, ItemStackSnapshot item) {
        return this.processWithMessage(
                event, item, event.cause().last(Audience.class).orElse(null), this.locale(event.cause()));
    }

    protected Optional<ItemStackSnapshot> processWithMessage(
            Event event, ItemStackSnapshot item, @Nullable Audience audience, Locale locale) {
        final var itemStack = item.createStack();
        final var components = Lists.<Component>newArrayList();
        final var finalResult = this.process(
                event,
                item,
                rule -> {
                    if (Objects.nonNull(audience)) components.add(this.ruleCancelledMessage(rule, itemStack, locale));
                },
                (rule, result) -> {
                    if (result.isEmpty() || Objects.isNull(audience)) return;
                    components.add(
                            this.ruleUpdateMessage(rule, itemStack, result.get().createStack(), locale));
                });
        if (Objects.nonNull(audience) && !components.isEmpty())
            audience.sendMessage(Component.join(JoinConfiguration.newlines(), components));
        return finalResult;
    }

    protected Optional<BlockSnapshot> processWithMessage(
            Event event, BlockSnapshot block, @Nullable Audience audience, Locale locale) {
        final var components = Lists.<Component>newArrayList();
        final var type = block.state().type();
        final var finalResult = this.process(
                event,
                block,
                rule -> components.add(
                        GlobalTranslator.render(rule.cancelledMessage().args(rule, this, type), locale)),
                (rule, result) -> {
                    if (result.isEmpty()) return;
                    components.add(GlobalTranslator.render(
                            rule.updatedMessage()
                                    .args(rule, this, type, result.get().state().type()),
                            locale));
                });
        if (Objects.nonNull(audience) && !components.isEmpty())
            audience.sendMessage(Component.join(JoinConfiguration.newlines(), components));
        return finalResult;
    }

    protected Optional<ItemStackSnapshot> process(
            Event event,
            ItemStackSnapshot item,
            Consumer<RestrictionRule> onCancelled,
            BiConsumer<RestrictionRule, Optional<ItemStackSnapshot>> onProcessed) {
        return this.process(
                event,
                item.toContainer(),
                item.type().key(RegistryTypes.ITEM_TYPE),
                onCancelled,
                onProcessed,
                view -> Sponge.dataManager().deserialize(ItemStackSnapshot.class, view));
    }

    protected Optional<BlockSnapshot> process(
            Event event,
            BlockSnapshot block,
            Consumer<RestrictionRule> onCancelled,
            BiConsumer<RestrictionRule, Optional<BlockSnapshot>> onProcessed) {
        return this.process(
                event,
                block.toContainer(),
                block.state().type().key(RegistryTypes.BLOCK_TYPE),
                onCancelled,
                onProcessed,
                view -> Sponge.dataManager().deserialize(BlockSnapshot.class, view));
    }

    protected <T> Optional<T> process(
            Event event,
            DataView view,
            ResourceKey objectType,
            Consumer<RestrictionRule> onCancelled,
            BiConsumer<RestrictionRule, Optional<T>> onProcessed,
            Function<DataView, Optional<T>> translator) {
        final var cause = event.cause();
        final var world =
                cause.last(Locatable.class).map(Locatable::serverLocation).map(Location::world);
        if (world.isEmpty()) return translator.apply(view);
        final var subject = cause.last(Subject.class).orElse(null);
        var finalView = Optional.<DataView>empty();
        for (RestrictionRule rule : predicateService
                .rules(predicateService.predicates(objectType))
                .sorted(RulePredicateService.PRIORITY_ASC)
                .toList()) {
            Optional<DataView> processed =
                    processRule(rule, event, view, world.get(), subject, onCancelled, onProcessed, translator);
            if (processed.isPresent()) finalView = processed;
        }
        return finalView.flatMap(translator);
    }

    protected <T> Optional<DataView> processRule(
            final RestrictionRule rule,
            final Event event,
            DataView view,
            final ServerWorld world,
            final @Nullable Subject subject,
            Consumer<RestrictionRule> onCancelled,
            BiConsumer<RestrictionRule, Optional<T>> onProcessed,
            Function<DataView, Optional<T>> translator) {
        if (rule.onlyPlayer() && !(subject instanceof ServerPlayer)) return Optional.empty();
        Optional<QueryResult> query = restrictionService.query(rule, view, world, this, subject);
        if (query.isEmpty()) return Optional.empty();
        if (rule.needCancel() && event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
            onCancelled.accept(rule);
        }
        Optional<DataView> finalView = query.flatMap(result ->
                rule.updateExpression().map(it -> it.update(result, view).process(view)));
        if (finalView.isEmpty()) return Optional.empty();
        onProcessed.accept(rule, translator.apply(view));
        return finalView;
    }
}
