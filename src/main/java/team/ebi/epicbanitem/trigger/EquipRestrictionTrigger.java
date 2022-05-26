/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.Locale;
import java.util.Optional;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.world.Locatable;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.translation.GlobalTranslator;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.AbstractRestrictionTrigger;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RulePredicateService;

@Singleton
public class EquipRestrictionTrigger extends AbstractRestrictionTrigger {

    @Inject
    private RulePredicateService predicateService;

    @Inject
    private RestrictionService restrictionService;

    public EquipRestrictionTrigger() {
        super(EpicBanItem.key("equip"));
    }

    @Listener
    public void onChangeEntityEquipment(
            final ChangeEntityEquipmentEvent event,
            @Last Locatable locatable,
            @Last Equipable equipable,
            @Getter("transaction") Transaction<ItemStackSnapshot> transaction) {
        final var item = transaction.finalReplacement();
        if (item.isEmpty()) return;
        final var itemStack = item.createStack();
        final var subject = event.cause().last(Subject.class).orElse(null);
        final var audience = event.cause().last(Audience.class);
        final var localeSource = event.cause().last(LocaleSource.class);
        final var locale = localeSource.map(LocaleSource::locale).orElse(Locale.getDefault());
        final var container = item.toContainer();
        final var itemType = item.type().key(RegistryTypes.ITEM_TYPE);
        final var predicates = predicateService.predicates(itemType);
        final var rules = predicateService
                .rules(predicates)
                .filter(it -> predicates.contains(it.predicate()))
                .sorted(RulePredicateService.PRIORITY_ASC)
                .toList();
        final var components = Lists.<Component>newArrayList();
        final var world = locatable.serverLocation().world();
        for (RestrictionRule rule : rules) {
            if (rule.needCancel()) {
                event.setCancelled(true);
                components.add(GlobalTranslator.render(rule.canceledMessage().args(rule, this, itemStack), locale));
            }
            Optional<ItemStackSnapshot> result = restrictionService
                    .restrict(rule, container, world, this, subject)
                    .flatMap(it -> restrictionService.processedItem(container, it));
            if (result.isEmpty()) continue;
            transaction.setCustom(result.get());
            components.add(GlobalTranslator.render(
                    rule.updatedMessage()
                            .args(rule, this, itemStack, result.get().createStack()),
                    locale));
        }

        audience.ifPresent(it -> it.sendMessage(Component.join(JoinConfiguration.newlines(), components)));
    }
}
