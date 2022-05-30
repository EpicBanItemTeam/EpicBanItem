/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RulePredicateService;
import team.ebi.epicbanitem.util.EventUtils;
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

    protected Optional<BlockSnapshot> process(Event event, BlockSnapshot block) {
        final var cause = event.cause();
        return this.process(event, block, cause.last(Audience.class).orElse(null), EventUtils.locale(cause));
    }

    protected Optional<ItemStackSnapshot> process(Event event, ItemStackSnapshot item) {
        final var cause = event.cause();
        return this.process(event, item, cause.last(Audience.class).orElse(null), EventUtils.locale(cause));
    }

    protected Optional<ItemStackSnapshot> process(
            Event event, ItemStackSnapshot item, @Nullable Audience audience, Locale locale) {
        return this.process(event, item, audience, locale, true);
    }

    protected Optional<ItemStackSnapshot> process(
            Event event,
            ItemStackSnapshot item,
            @Nullable Audience audience,
            Locale locale,
            final boolean shouldCancel) {
        final var itemStack = item.createStack();
        final var components = Lists.<Component>newArrayList();
        final var finalResult = this.process(
                event,
                item,
                shouldCancel,
                rule -> {
                    if (Objects.nonNull(audience)) {
                        components.add(GlobalTranslator.render(
                                rule.cancelledMessage().args(rule, description(), itemStack, this), locale));
                    }
                },
                (rule, result) -> {
                    if (result.isEmpty() || Objects.isNull(audience)) {
                        return;
                    }
                    components.add(GlobalTranslator.render(
                            rule.updatedMessage()
                                    .args(
                                            rule,
                                            description(),
                                            itemStack,
                                            result.get().createStack()),
                            locale));
                });
        if (Objects.nonNull(audience) && !components.isEmpty()) {
            audience.sendMessage(Component.join(JoinConfiguration.newlines(), components));
        }
        return finalResult;
    }

    protected Optional<BlockSnapshot> process(
            Event event, BlockSnapshot block, @Nullable Audience audience, Locale locale) {
        return this.process(event, block, audience, locale, true);
    }

    protected Optional<BlockSnapshot> process(
            Event event, BlockSnapshot block, @Nullable Audience audience, Locale locale, final boolean shouldCancel) {
        final var components = Lists.<Component>newArrayList();
        final var type = block.state().type();
        final var finalResult = this.process(
                event,
                block,
                shouldCancel,
                rule -> {
                    if (Objects.nonNull(audience)) {
                        components.add(GlobalTranslator.render(
                                rule.cancelledMessage().args(rule, description(), type, this), locale));
                    }
                },
                (rule, result) -> {
                    if (result.isEmpty() || Objects.isNull(audience)) {
                        return;
                    }
                    components.add(GlobalTranslator.render(
                            rule.updatedMessage()
                                    .args(
                                            rule,
                                            description(),
                                            type,
                                            result.get().state().type()),
                            locale));
                });
        if (Objects.nonNull(audience) && !components.isEmpty()) {
            audience.sendMessage(Component.join(JoinConfiguration.newlines(), components));
        }
        return finalResult;
    }

    private Optional<ItemStackSnapshot> process(
            final Event event,
            final ItemStackSnapshot item,
            final boolean shouldCancel,
            final Consumer<RestrictionRule> onCancelled,
            final BiConsumer<RestrictionRule, Optional<ItemStackSnapshot>> onProcessed) {
        return this.process(
                event,
                item.toContainer(),
                item.type().key(RegistryTypes.ITEM_TYPE),
                shouldCancel,
                onCancelled,
                onProcessed,
                view -> Sponge.dataManager().deserialize(ItemStackSnapshot.class, view));
    }

    private Optional<BlockSnapshot> process(
            final Event event,
            final BlockSnapshot block,
            final boolean shouldCancel,
            final Consumer<RestrictionRule> onCancelled,
            final BiConsumer<RestrictionRule, Optional<BlockSnapshot>> onProcessed) {
        return ItemUtils.fromBlock(block)
                .map(DataSerializable::toContainer)
                .flatMap(it -> this.process(
                        event,
                        it,
                        block.state().type().key(RegistryTypes.BLOCK_TYPE),
                        shouldCancel,
                        onCancelled,
                        onProcessed,
                        view -> Sponge.dataManager().deserialize(BlockSnapshot.class, view)));
    }

    private <T> Optional<T> process(
            final Event event,
            final DataView view,
            final ResourceKey objectType,
            final boolean shouldCancel,
            final Consumer<RestrictionRule> onCancelled,
            final BiConsumer<RestrictionRule, Optional<T>> onProcessed,
            final Function<DataView, Optional<T>> translator) {
        final var cause = event.cause();
        final var world =
                cause.last(Locatable.class).map(Locatable::serverLocation).map(Location::world);
        if (world.isEmpty()) {
            return translator.apply(view);
        }
        final var subject = cause.last(Subject.class).orElse(null);
        var finalView = Optional.<DataView>empty();
        for (RestrictionRule rule : predicateService
                .rules(predicateService.predicates(objectType))
                .sorted(RulePredicateService.PRIORITY_ASC)
                .toList()) {
            Optional<DataView> processed = processRule(
                    rule, event, view, world.get(), subject, shouldCancel, onCancelled, onProcessed, translator);
            if (processed.isPresent()) {
                finalView = processed;
            }
        }
        return finalView.flatMap(translator);
    }

    private <T> Optional<DataView> processRule(
            final RestrictionRule rule,
            final Event event,
            final DataView view,
            final ServerWorld world,
            final @Nullable Subject subject,
            final boolean shouldCancel,
            Consumer<RestrictionRule> onCancelled,
            BiConsumer<RestrictionRule, Optional<T>> onProcessed,
            Function<DataView, Optional<T>> translator) {
        if (rule.onlyPlayer() && !(subject instanceof ServerPlayer)) {
            return Optional.empty();
        }
        Optional<QueryResult> query = restrictionService.query(rule, view, world, this, subject);
        if (query.isEmpty()) {
            return Optional.empty();
        }
        if (rule.needCancel() && event instanceof Cancellable cancellable) {
            if (shouldCancel) cancellable.setCancelled(true);
            onCancelled.accept(rule);
        }
        Optional<DataView> finalView = query.flatMap(result -> rule.updateExpression()
                .map(it -> it.update(result, view))
                .filter(Predicate.not(Map::isEmpty))
                .map(it -> it.process(view)));
        if (finalView.isEmpty()) {
            return Optional.empty();
        }
        onProcessed.accept(rule, translator.apply(view));
        return finalView;
    }

    protected <T extends InteractEvent> void handleInteract(
            T event, Equipable equipable, HandType hand, ItemStackSnapshot item) {
        EquipmentType equipment = EquipmentTypes.registry().value(hand.key(RegistryTypes.HAND_TYPE));
        Optional<Slot> slot = equipable.equipment().slot(equipment);
        if (slot.isEmpty()) {
            return;
        }
        this.process(event, item).ifPresent(it -> slot.get().set(it.createStack()));
    }

    protected void handleInventory(
            final Inventory inventory, final Event event, final Audience audience, final Locale locale) {
        inventory.slots().stream().filter(it -> it.freeCapacity() == 0).forEach(slot -> this.process(
                        event, slot.peek().createSnapshot(), audience, locale)
                .ifPresent(it -> slot.set(it.createStack())));
    }
}
