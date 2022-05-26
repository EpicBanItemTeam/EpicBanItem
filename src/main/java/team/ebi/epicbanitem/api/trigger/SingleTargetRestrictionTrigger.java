/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.trigger;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;

import com.google.inject.Inject;
import team.ebi.epicbanitem.api.RestrictionService;
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

    protected DataView process(
            Event event,
            DataView view,
            ResourceKey objectType,
            Consumer<RestrictionRule> onCancel,
            BiConsumer<RestrictionRule, DataView> onProcessed) {
        final var cause = event.cause();
        final var world =
                cause.last(Locatable.class).map(Locatable::serverLocation).map(Location::world);
        if (world.isEmpty()) return view;
        final var subject = cause.last(Subject.class).orElse(null);
        final var predicates = predicateService.predicates(objectType);
        for (RestrictionRule rule : predicateService
                .rules(predicates)
                .filter(it -> predicates.contains(it.predicate()))
                .sorted(RulePredicateService.PRIORITY_ASC)
                .toList()) {
            if (rule.needCancel() && event instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
                onCancel.accept(rule);
            }
            DataView finalView = view;
            Optional<DataView> result = restrictionService
                    .restrict(rule, view, world.get(), this, subject)
                    .map(it -> it.process(finalView));
            if (result.isEmpty()) continue;
            view = result.get();
            onProcessed.accept(rule, view);
        }
        return view;
    }
}
