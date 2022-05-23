/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import java.util.Map;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.data.persistence.DataSerializable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.UpdateExpression;

public interface RestrictionRule extends ResourceKeyed, ComponentLike, DataSerializable {
    /**
     * @return The priority of current rule (ASC, lower first). <br> Default: 10
     */
    int priority();

    boolean needCancel();

    boolean defaultWorldState();

    boolean defaultTriggerState();

    States<ResourceKey> worldStates();

    boolean worldState(ResourceKey key);

    boolean triggerState(ResourceKey key);

    States<ResourceKey> triggerStates();

    QueryExpression queryExpression();

    @Nullable
    UpdateExpression updateExpression();

    /**
     * <li>"minecraft:*" will try to match rule on all minecraft objects
     * <li>"*:*" will try to match rule on all objects
     * <li>"minecraft:dirt" will only try to match rule when target is dirt
     *
     * @return The id filter for performance.
     */
    ResourceKey predicate();

    /**
     * @return Translatable component with args:
     * <li>0: rule
     * <li>1: trigger
     * <li>2: origin object name
     * <li>3: final object name
     */
    @Contract(pure = true)
    TranslatableComponent updatedMessage();

    /**
     * @return Translatable component with args:
     * <li>0: rule
     * <li>1: trigger
     * <li>2: origin object name
     */
    @Contract(pure = true)
    TranslatableComponent canceledMessage();

    @Override
    @NotNull
    Component asComponent();

    interface States<K> extends Map<K, Boolean>, ComponentLike {
        ComponentLike key(K key);

        void update(boolean defaultState);

        @Override
        default @NotNull Component asComponent() {
            return Component.join(
                    JoinConfiguration.separator(Component.space()),
                    keySet().stream()
                            .map(key -> Component.text()
                                    .append(key(key))
                                    .color(Boolean.TRUE.equals(get(key)) ? NamedTextColor.GREEN : NamedTextColor.RED)
                                    .clickEvent(SpongeComponents.executeCallback(cause -> put(key, !get(key))))
                                    .build())
                            .toList());
        }
    }
}
