/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.rule;

import java.util.Optional;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.data.persistence.DataSerializable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
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

    @Contract(pure = true)
    RestrictionRule priority(int value);

    boolean needCancel();

    @Contract(pure = true)
    RestrictionRule needCancel(boolean value);

    States worldStates();

    @Contract(pure = true)
    RestrictionRule worldStates(WorldStates states);

    States triggerStates();

    @Contract(pure = true)
    RestrictionRule triggerStates(TriggerStates states);

    QueryExpression queryExpression();

    @Contract(pure = true)
    RestrictionRule queryExpression(QueryExpression value);

    Optional<UpdateExpression> updateExpression();

    @Contract(pure = true)
    @Nullable
    RestrictionRule updateExpression(UpdateExpression value);

    /**
     * <li>"minecraft:*" will try to match rule on all minecraft objects
     * <li>"*:*" will try to match rule on all objects
     * <li>"minecraft:dirt" will only try to match rule when target is dirt
     *
     * @return The id filter for performance.
     */
    ResourceKey predicate();

    @Contract(pure = true)
    RestrictionRule predicate(ResourceKey value);

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
    TranslatableComponent cancelledMessage();

    @Override
    @NotNull
    Component asComponent();
}
