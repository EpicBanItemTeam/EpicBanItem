/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api;

import java.util.Optional;

import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;

import com.google.common.collect.Sets;
import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.trigger.RestrictionTrigger;
import team.ebi.epicbanitem.rule.RestrictionServiceImpl;

@ImplementedBy(RestrictionServiceImpl.class)
public interface RestrictionService {

    default boolean shouldBypass(Subject subject, RestrictionRule rule, RestrictionTrigger trigger) {
        return subject.hasPermission(
                EpicBanItem.permission("bypass." + rule),
                Sets.newHashSet(new Context(
                        RestrictionTrigger.CONTEXT_KEY, trigger.key().asString())));
    }

    default Optional<QueryResult> query(
            RestrictionRule rule,
            DataView view,
            ServerWorld world,
            RestrictionTrigger trigger,
            @Nullable Subject subject) {
        if (!rule.triggerStates().getOrDefault(trigger.key())) {
            return Optional.empty();
        }
        if (!rule.worldStates().getOrDefault(world.key())) {
            return Optional.empty();
        }
        //        if (Objects.nonNull(subject) && shouldBypass(subject, rule, trigger)) {
        //            return Optional.empty();
        //        }
        return rule.queryExpression().query(view);
    }
}
