/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import com.google.common.collect.Lists;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.util.Components;

public interface ProcessHandler<T> {
    Optional<T> translate(DataView view);

    @FunctionalInterface
    interface CancellableHandler {
        CancellableHandler CANCEL_EVENT = (event) -> {
            if (event instanceof Cancellable cancellable) cancellable.setCancelled(true);
        };

        void cancel(Event event);
    }

    interface Item extends ProcessHandler<ItemStackSnapshot> {
        @Override
        default Optional<ItemStackSnapshot> translate(DataView view) {
            return Sponge.dataManager().deserialize(ItemStackSnapshot.class, view);
        }

        class Impl implements Item {}

        class Cancellable implements CancellableHandler, Item {
            private final CancellableHandler handler;

            public Cancellable(CancellableHandler handler) {
                this.handler = handler;
            }

            @Override
            public void cancel(Event event) {
                handler.cancel(event);
            }
        }

        class Message extends ProcessHandler.Message<ItemStackSnapshot> implements Item {
            public Message(ItemStackSnapshot originObject, RestrictionTrigger trigger) {
                super(originObject, trigger);
            }

            @Override
            protected ComponentLike component(ItemStackSnapshot obj) {
                return obj.createStack();
            }
        }

        class MessageCancellable extends Message implements Item, CancellableHandler {
            private final CancellableHandler handler;

            public MessageCancellable(
                    ItemStackSnapshot originObject,
                    RestrictionTrigger trigger,
                    ProcessHandler.CancellableHandler handler) {
                super(originObject, trigger);
                this.handler = handler;
            }

            @Override
            public void cancel(Event event) {
                handler.cancel(event);
            }
        }
    }

    abstract class Message<T> implements ProcessHandler<T>, ComponentLike {
        protected final ComponentLike originComponent;
        protected final RestrictionTrigger trigger;
        protected final List<Component> components;

        public Message(T originObject, RestrictionTrigger trigger) {
            this(originObject, trigger, Lists.newArrayList());
        }

        public Message(T originObject, RestrictionTrigger trigger, List<Component> components) {
            this.originComponent = component(originObject);
            this.trigger = trigger;
            this.components = components;
        }

        protected abstract ComponentLike component(T obj);

        @Override
        public @NotNull Component asComponent() {
            return Component.join(JoinConfiguration.newlines(), components);
        }

        public void sendTo(Audience audience) {
            if (!components.isEmpty()) audience.sendMessage(this);
        }

        public void cancelledMessages(List<RestrictionRule> rules) {
            final var commonRules = Lists.<RestrictionRule>newArrayList();
            final var description = trigger.description();
            for (final var rule : rules) {
                final var component = rule.cancelledMessage();
                if (component.isPresent()) {
                    component
                            .map(it -> it.args(rule, description, originComponent, trigger))
                            .ifPresent(components::add);
                } else {
                    commonRules.add(rule);
                }
            }
            components.add(Components.RULE_CANCELLED.args(
                    Component.join(JoinConfiguration.commas(true), commonRules),
                    description,
                    originComponent,
                    trigger));
        }

        public void updatedMessages(List<RestrictionRule> rules, @Nullable T result) {
            final var commonRules = Lists.<RestrictionRule>newArrayList();
            final var description = trigger.description();
            final var finalComponent = component(result);
            for (final var rule : rules) {
                final var component = rule.updatedMessage();
                if (component.isPresent()) {
                    component
                            .map(it -> it.args(rule, description, originComponent, finalComponent))
                            .ifPresent(components::add);
                } else {
                    commonRules.add(rule);
                }
            }
            if (!commonRules.isEmpty())
                components.add(Components.RULE_UPDATED.args(
                        Component.join(JoinConfiguration.commas(true), commonRules),
                        description,
                        originComponent,
                        finalComponent));
        }
    }
}
