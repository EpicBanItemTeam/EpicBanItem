/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util;

import java.util.Objects;

import org.spongepowered.api.util.Tristate;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.rule.RestrictionRule;

public final class StaticRestrictionRuleRenderer {

    private static final Component COLON = Component.text(": ");

    public static Component renderRule(RestrictionRule rule) {
        final var ruleKeyString = rule.key().asString();
        final var components = Lists.<Component>newArrayList();
        components.add(renderKey(Component.translatable("epicbanitem.ui.rule.title.key"))
                .append(rule.asComponent()
                        .hoverEvent(Component.text(ruleKeyString)
                                .append(Component.newline())
                                .append(Component.translatable("epicbanitem.ui.rule.title.description")
                                        .args(Component.text(ruleKeyString))))
                        .clickEvent(ClickEvent.copyToClipboard(ruleKeyString))));

        components.add(renderKey(Component.translatable("epicbanitem.ui.rule.priority.key"))
                .append(Component.text(rule.priority())));

        components.add(renderWorldStates(rule.worldStates()));
        components.add(renderTriggerStates(rule.triggerStates()));

        return Component.join(JoinConfiguration.newlines(), components);
    }

    private static @NotNull Component renderKey(Component key) {
        return key.append(COLON);
    }

    private static @NotNull Component renderWorldStates(RestrictionRule.States states) {
        return renderKey(Component.translatable("epicbanitem.ui.rule.worldStates.key"))
                .append(renderRuleStates(states));
    }

    private static @NotNull Component renderTriggerStates(RestrictionRule.States states) {
        return renderKey(Component.translatable("epicbanitem.ui.rule.triggerStates.key"))
                .append(renderRuleStates(states));
    }

    @Contract(pure = true)
    private static @NotNull Component renderRuleStates(RestrictionRule.@NotNull States states) {
        return Component.text()
                .append(Component.translatable("epicbanitem.ui.rule.defaultState")
                        .color(states.defaultState() ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .hoverEvent(Component.translatable("epicbanitem.ui.rule.defaultState.description")))
                .append(Component.newline())
                .append(Component.join(
                        JoinConfiguration.separator(Component.text("  ")),
                        states.keySet().stream()
                                .map(key -> {
                                    final var tristate = states.get(key);
                                    final var builder = Component.text();
                                    builder.append(states.key(key))
                                            .color(
                                                    Objects.requireNonNullElse(
                                                                    tristate.asNullableBoolean(), states.defaultState())
                                                            ? NamedTextColor.GREEN
                                                            : NamedTextColor.RED)
                                            .hoverEvent(Component.text(key.asString()))
                                            .clickEvent(ClickEvent.copyToClipboard(key.asString()));
                                    if (tristate.equals(Tristate.UNDEFINED)) builder.decorate(TextDecoration.ITALIC);
                                    return builder.build();
                                })
                                .toList()))
                .build();
    }
}
