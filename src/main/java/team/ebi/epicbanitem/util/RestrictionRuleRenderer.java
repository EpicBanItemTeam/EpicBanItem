/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Objects;

import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Lists;
import joptsimple.internal.Strings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.States;
import team.ebi.epicbanitem.util.data.DataViewRenderer;

public final class RestrictionRuleRenderer {

    private static final Component COLON = Component.text(": ");
    private static final Component DIVIDER_TOP = Component.text(Strings.repeat('━', 22));
    private static final Component DIVIDER_BOTTOM = Component.text(Strings.repeat('━', 22));

    public static Component renderRule(RestrictionRule rule) throws IOException {
        final var ruleKeyString = rule.key().asString();
        final var components = Lists.<Component>newArrayList();
        components.add(DIVIDER_TOP);
        components.add(renderKey(Component.translatable("epicbanitem.ui.rule.title.key"))
                .append(rule.asComponent())
                .hoverEvent(Component.text(ruleKeyString)
                        .append(Component.newline())
                        .append(Component.translatable("epicbanitem.ui.rule.title.description")
                                .args(Component.text(ruleKeyString))))
                .clickEvent(ClickEvent.copyToClipboard(ruleKeyString)));

        components.add(renderKey(Component.translatable("epicbanitem.ui.rule.predicate.key"))
                .append(Component.text(rule.predicate().asString()))
                .hoverEvent(Component.translatable("epicbanitem.ui.rule.predicate.description"))
                .clickEvent(ClickEvent.suggestCommand(
                        MessageFormat.format("/{0} set {1} predicate ", EpicBanItem.NAMESPACE, ruleKeyString))));

        components.add(renderKey(Component.translatable("epicbanitem.ui.rule.priority.key"))
                .append(Component.text(rule.priority()))
                .hoverEvent(Component.translatable("epicbanitem.ui.rule.priority.description"))
                .clickEvent(ClickEvent.suggestCommand(
                        MessageFormat.format("/{0} set {1} priority ", EpicBanItem.NAMESPACE, ruleKeyString))));

        components.add(Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.cancel.key")
                        .color(rule.needCancel() ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.cancel.description"))
                        .clickEvent(ClickEvent.suggestCommand(MessageFormat.format(
                                "/{0} set {1} cancel {2}", EpicBanItem.NAMESPACE, ruleKeyString, !rule.needCancel()))))
                .append(Component.space())
                .append(Component.translatable("epicbanitem.ui.rule.onlyPlayer.key")
                        .color(rule.onlyPlayer() ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.onlyPlayer.description"))
                        .clickEvent(ClickEvent.suggestCommand(MessageFormat.format(
                                "/{0} set {1} only-player {2}",
                                EpicBanItem.NAMESPACE, ruleKeyString, !rule.onlyPlayer()))))
                .build());

        components.add(renderWorldStates(ruleKeyString, rule.worldStates()));
        components.add(renderTriggerStates(ruleKeyString, rule.triggerStates()));

        // TODO Click suggest command
        final var format = DataFormats.JSON.get();
        final var updateExpression = rule.updateExpression();
        final var query = rule.queryExpression().toContainer();
        final var update = updateExpression.map(DataSerializable::toContainer);
        components.add(Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.query.key")
                        .hoverEvent(Component.join(
                                JoinConfiguration.newlines(),
                                DataViewRenderer.render(query).stream()
                                        .limit(25)
                                        .toList()))
                        .clickEvent(ClickEvent.suggestCommand(MessageFormat.format(
                                "/{0} set {1} query {2}", EpicBanItem.NAMESPACE, ruleKeyString, format.write(query)))))
                .append(Component.space())
                .append(Component.translatable("epicbanitem.ui.rule.update.key")
                        .hoverEvent(
                                updateExpression.isEmpty()
                                        ? Component.text("null")
                                        : Component.join(
                                                JoinConfiguration.newlines(),
                                                DataViewRenderer.render(update.get()).stream()
                                                        .limit(25)
                                                        .toList()))
                        .clickEvent(ClickEvent.suggestCommand(MessageFormat.format(
                                "/{0} set {1} update {2}",
                                EpicBanItem.NAMESPACE,
                                ruleKeyString,
                                update.isPresent() ? format.write(update.get()) : "{}"))))
                .build());

        components.add(Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.updateMessage.key")
                        .hoverEvent(Component.text()
                                .append(rule.updatedMessage().orElse(Components.RULE_UPDATED))
                                .append(Component.newline())
                                .append(Component.translatable("epicbanitem.ui.rule.updateMessage.description")
                                        .args(Component.text(ruleKeyString)))
                                .build()))
                .append(Component.space())
                .append(Component.translatable("epicbanitem.ui.rule.cancelMessage.key")
                        .hoverEvent(Component.text()
                                .append(rule.cancelledMessage().orElse(Components.RULE_CANCELLED))
                                .append(Component.newline())
                                .append(Component.translatable("epicbanitem.ui.rule.cancelMessage.description")
                                        .args(Component.text(ruleKeyString)))
                                .build()))
                .build());
        components.add(DIVIDER_BOTTOM);
        return Component.join(JoinConfiguration.newlines(), components);
    }

    private static @NotNull Component renderKey(Component key) {
        return key.append(COLON);
    }

    private static @NotNull Component renderWorldStates(String rule, States states) {
        return renderKey(Component.translatable("epicbanitem.ui.rule.worldStates.key"))
                .append(renderRuleStates(rule, states, "world"));
    }

    private static @NotNull Component renderTriggerStates(String rule, States states) {
        return renderKey(Component.translatable("epicbanitem.ui.rule.triggerStates.key"))
                .append(renderRuleStates(rule, states, "trigger"));
    }

    @Contract(pure = true)
    private static @NotNull Component renderRuleStates(
            final String rule, final @NotNull States states, final String stateName) {
        final var defaultState = states.defaultState();
        return Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.defaultState")
                        .color(defaultState ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.defaultState.description"))
                        .clickEvent(ClickEvent.suggestCommand(MessageFormat.format(
                                "/{0} set {1} {2}-default {3}",
                                EpicBanItem.NAMESPACE, rule, stateName, !defaultState))))
                .append(Component.newline())
                .append(Component.join(
                        JoinConfiguration.separator(Component.space()),
                        states.keySet().stream()
                                .map(key -> {
                                    final var tristate = states.getOrDefault(key, Tristate.UNDEFINED);
                                    final var builder = Component.text();
                                    builder.append(states.key(key))
                                            .color(
                                                    Objects.requireNonNullElse(
                                                                    tristate.asNullableBoolean(), defaultState)
                                                            ? NamedTextColor.GREEN
                                                            : NamedTextColor.RED)
                                            .hoverEvent(states.description(key))
                                            .clickEvent(ClickEvent.suggestCommand(MessageFormat.format(
                                                    "/{0} set {1} {2} {3} {4}",
                                                    EpicBanItem.NAMESPACE,
                                                    rule,
                                                    stateName,
                                                    key,
                                                    (tristate.equals(Tristate.UNDEFINED)
                                                                    ? Tristate.fromBoolean(!defaultState)
                                                                    : Tristate.UNDEFINED)
                                                            .name()
                                                            .toLowerCase())));
                                    if (tristate.equals(Tristate.UNDEFINED)) builder.decorate(TextDecoration.ITALIC);
                                    return builder.build();
                                })
                                .toList()))
                .build();
    }
}
