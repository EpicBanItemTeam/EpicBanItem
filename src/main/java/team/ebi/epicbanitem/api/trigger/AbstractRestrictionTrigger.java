/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RulePredicateService;
import team.ebi.epicbanitem.util.ItemUtils;

public abstract class AbstractRestrictionTrigger implements RestrictionTrigger {

    private final ResourceKey key;

    @Inject
    private RulePredicateService predicateService;

    @Inject
    private RestrictionService restrictionService;

    protected AbstractRestrictionTrigger(ResourceKey key) {
        this.key = key;
        Sponge.eventManager()
                .registerListeners(
                        Sponge.pluginManager()
                                .plugin(EpicBanItem.NAMESPACE)
                                .orElseThrow(() -> new IllegalStateException("EpicBanItem haven't been loaded")),
                        this);
    }

    @Override
    public @NotNull ResourceKey key() {
        return key;
    }

    @Override
    public @NotNull Component asComponent() {
        final var resourceKey = key();
        final var key = EpicBanItem.NAMESPACE + ".trigger." + resourceKey;
        Component component = Component.text(resourceKey.asString());
        if (EpicBanItem.translations.contains(key)) {
            component = Component.translatable(key);
        }
        return component.hoverEvent(description());
    }

    @Override
    public Component description() {
        return Component.translatable(EpicBanItem.NAMESPACE + ".trigger." + key() + ".description");
    }

    protected Optional<BlockSnapshot> processCancellable(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final BlockSnapshot block) {
        return this.processCancellable(
                event, world, subject, audience, block, ProcessHandler.CancellableHandler.CANCEL_EVENT);
    }

    protected Optional<ItemStackSnapshot> processCancellable(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final ItemStackSnapshot item) {
        return this.processCancellable(
                event, world, subject, audience, item, ProcessHandler.CancellableHandler.CANCEL_EVENT);
    }

    protected Optional<BlockSnapshot> processCancellable(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final BlockSnapshot block,
            final ProcessHandler.CancellableHandler cancellable) {
        return this.process(event, world, subject, audience, block, (ProcessHandler.Block)
                (Objects.isNull(audience)
                        ? new ProcessHandler.Block.Cancellable(cancellable)
                        : new ProcessHandler.Block.MessageCancellable(block, this, cancellable)));
    }

    protected Optional<ItemStackSnapshot> processCancellable(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final ItemStackSnapshot item,
            final ProcessHandler.CancellableHandler cancellable) {
        return this.process(event, world, subject, audience, item, (ProcessHandler.Item)
                (Objects.isNull(audience)
                        ? new ProcessHandler.Item.Cancellable(cancellable)
                        : new ProcessHandler.Item.MessageCancellable(item, this, cancellable)));
    }

    protected Optional<BlockSnapshot> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final BlockSnapshot block) {
        return this.process(
                event,
                world,
                subject,
                audience,
                block,
                Objects.isNull(audience)
                        ? new ProcessHandler.Block.Impl()
                        : new ProcessHandler.Block.Message(block, this));
    }

    protected Optional<ItemStackSnapshot> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final ItemStackSnapshot item) {
        return this.process(
                event,
                world,
                subject,
                audience,
                item,
                Objects.isNull(audience)
                        ? new ProcessHandler.Item.Impl()
                        : new ProcessHandler.Item.Message(item, this));
    }

    protected Optional<BlockSnapshot> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final BlockSnapshot block,
            final ProcessHandler.Block handler) {
        final var processed = this.process(event, world, subject, block, handler);
        if (Objects.nonNull(audience) && handler instanceof ProcessHandler.Message message) message.sendTo(audience);
        return processed;
    }

    protected Optional<ItemStackSnapshot> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final @Nullable Audience audience,
            final ItemStackSnapshot item,
            final ProcessHandler.Item handler) {
        final var processed = this.process(event, world, subject, item, handler);
        if (Objects.nonNull(audience) && handler instanceof ProcessHandler.Message message) message.sendTo(audience);
        return processed;
    }

    protected Optional<BlockSnapshot> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final BlockSnapshot block,
            final ProcessHandler.Block handler) {
        return ItemUtils.fromBlock(block)
                .map(DataSerializable::toContainer)
                .flatMap(view -> this.process(
                        event, world, subject, block.state().type().key(RegistryTypes.BLOCK_TYPE), view, handler));
    }

    protected Optional<ItemStackSnapshot> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final ItemStackSnapshot item,
            final ProcessHandler.Item handler) {
        return this.process(
                event, world, subject, item.type().key(RegistryTypes.ITEM_TYPE), item.toContainer(), handler);
    }

    protected <T> Optional<T> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final ResourceKey objectType,
            final DataView view,
            final ProcessHandler.Message<T> handler) {
        return this.process(event, world, subject, objectType, view, (ProcessHandler<T>) handler);
    }

    protected <T> Optional<T> process(
            final Event event,
            final ServerWorld world,
            final @Nullable Subject subject,
            final ResourceKey objectType,
            final DataView view,
            final ProcessHandler<T> handler) {
        final var cancelled = new ArrayList<RestrictionRule>();
        final var updated = new ArrayList<RestrictionRule>();
        var resultView = Optional.<DataView>empty();
        for (final var rule : predicateService
                .rules(predicateService.predicates(objectType))
                .sorted(RulePredicateService.PRIORITY_ASC)
                .filter(it -> !it.onlyPlayer() || subject instanceof ServerPlayer)
                .toList()) {
            final var query = restrictionService.query(rule, view, world, this, subject);
            if (query.isEmpty()) continue;
            if (rule.needCancel() && handler instanceof ProcessHandler.CancellableHandler cancellableHandler) {
                cancellableHandler.cancel(event);
                cancelled.add(rule);
            }
            final var prevView = resultView.orElse(view);
            final var currView = query.flatMap(result -> rule.updateExpression()
                    .map(it -> it.update(result, prevView))
                    .filter(Predicate.not(Map::isEmpty))
                    .map(it -> it.process(prevView)));
            if (currView.isEmpty()) continue;
            resultView = currView;
            updated.add(rule);
        }
        Optional<T> result = resultView.flatMap(handler::translate);
        if (handler instanceof ProcessHandler.Message<T> message) {
            if (!cancelled.isEmpty()) message.cancelledMessages(cancelled);
            if (!updated.isEmpty() && result.isPresent()) message.updatedMessages(updated, result.get());
        }
        return result;
    }

    protected Optional<Slot> slotFromHand(final Equipable equipable, final HandType hand) {
        return equipable.equipment().slot((EquipmentType)
                EquipmentTypes.registry().value(hand.key(RegistryTypes.EQUIPMENT_TYPE)));
    }

    protected <T extends DropItemEvent> void handleDrop(T event) {}
}
