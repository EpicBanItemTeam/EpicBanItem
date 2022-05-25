/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.util.Objects;

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
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.States;
import team.ebi.epicbanitem.util.data.DataViewRenderer;

public final class RestrictionRuleRenderer {

    private static final Component COLON = Component.text(": ");
    private static final Component DIVIDER_TOP = Component.text(Strings.repeat('━', 15));
    private static final Component DIVIDER_BOTTOM = Component.text(Strings.repeat('━', 15));

    public static Component renderRule(RestrictionRule rule) {
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
                        "/" + EpicBanItem.NAMESPACE + " set predicate " + ruleKeyString + " " + rule.predicate())));

        components.add(renderKey(Component.translatable("epicbanitem.ui.rule.priority.key"))
                .append(Component.text(rule.priority()))
                .hoverEvent(Component.translatable("epicbanitem.ui.rule.priority.description"))
                .clickEvent(ClickEvent.suggestCommand(
                        "/" + EpicBanItem.NAMESPACE + " set priority " + ruleKeyString + " " + rule.priority())));

        components.add(renderWorldStates(ruleKeyString, rule.worldStates()));
        components.add(renderTriggerStates(ruleKeyString, rule.triggerStates()));

        // TODO Click suggest command
        UpdateExpression updateExpression = rule.updateExpression();
        components.add(Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.query.key")
                        .hoverEvent(Component.join(
                                JoinConfiguration.newlines(),
                                DataViewRenderer.render(rule.queryExpression().toContainer()).stream()
                                        .limit(25)
                                        .toList())))
                .append(Component.text("  "))
                .append(Component.translatable("epicbanitem.ui.rule.update.key")
                        .hoverEvent(
                                Objects.isNull(updateExpression)
                                        ? Component.text("null")
                                        : Component.join(
                                                JoinConfiguration.newlines(),
                                                DataViewRenderer.render(Objects.requireNonNull(updateExpression)
                                                                .toContainer())
                                                        .stream()
                                                        .limit(25)
                                                        .toList())))
                .build());

        components.add(Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.updateMessage.key")
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.updateMessage.description")
                                .args(Component.text(ruleKeyString))))
                .append(Component.text("  "))
                .append(Component.translatable("epicbanitem.ui.rule.cancelMessage.key")
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.cancelMessage.description")
                                .args(Component.text(ruleKeyString))))
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
    private static @NotNull Component renderRuleStates(String rule, @NotNull States states, String stateName) {
        return Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.defaultState")
                        .color(states.defaultState() ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.defaultState.description")))
                .append(Component.newline())
                .append(Component.join(
                        JoinConfiguration.separator(Component.text(" ")),
                        states.keySet().stream()
                                .map(key -> {
                                    final var tristate = states.getOrDefault(key, Tristate.UNDEFINED);
                                    final var builder = Component.text();
                                    builder.append(states.key(key))
                                            .color(
                                                    Objects.requireNonNullElse(
                                                                    tristate.asNullableBoolean(), states.defaultState())
                                                            ? NamedTextColor.GREEN
                                                            : NamedTextColor.RED)
                                            .hoverEvent(Component.text(key.asString()))
                                            .clickEvent(ClickEvent.suggestCommand("/" + EpicBanItem.NAMESPACE + " set "
                                                    + stateName + " " + rule + " " + key + " " + tristate));
                                    if (tristate.equals(Tristate.UNDEFINED)) builder.decorate(TextDecoration.ITALIC);
                                    return builder.build();
                                })
                                .toList()))
                .build();
    }
}
