/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.server.ServerWorld;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.AbstractRestrictionTrigger;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RulePredicateService;

public class UseRestrictionTrigger extends AbstractRestrictionTrigger {

    @Inject
    private RulePredicateService predicateService;

    @Inject
    private RestrictionService restrictionService;

    public UseRestrictionTrigger() {
        super(EpicBanItem.key("use"));
    }

    @Listener
    public void onInteractItem(
            InteractItemEvent.Secondary event,
            @Last Locatable locatable,
            @Last Equipable equipable,
            @Last LocaleSource localeSource) {
        EquipmentType equipment = EquipmentTypes.OFF_HAND.get();
        Optional<Slot> slot = equipable.equipment().slot(equipment);
        if (slot.isEmpty()) {
            return;
        }
        final var subject = event.cause().last(Subject.class).orElse(null);
        final var audience = event.cause().last(Audience.class);
        final var locale = localeSource.locale();
        final var itemStack = slot.get().peek();
        final var item = itemStack.createSnapshot();
        final var container = item.toContainer();
        final var itemType = item.type().key(RegistryTypes.ITEM_TYPE);
        Set<ResourceKey> predicates = predicateService.predicates(itemType);
        List<RestrictionRule> rules = predicateService.rulesWithPriority(itemType);
        List<Component> components = Lists.newArrayList();
        ServerWorld world = locatable.serverLocation().world();
        for (RestrictionRule rule : rules) {
            if (!predicates.contains(rule.predicate())) return;
            if (rule.needCancel()) {
                event.setCancelled(true);
                TranslatableComponent component = rule.canceledMessage();
                components.add(GlobalTranslator.render(component.args(rule, this, itemStack), locale));
            }
            Optional<ItemStackSnapshot> result = restrictionService
                    .restrict(rule, container, world, this, subject)
                    .flatMap(it -> Sponge.dataManager().deserialize(ItemStackSnapshot.class, it.process(container)));
            if (result.isEmpty()) {
                break;
            }
            slot.get().set(result.get().createStack());
            TranslatableComponent component = rule.updatedMessage();
            components.add(GlobalTranslator.render(
                    component.args(rule, this, itemStack, result.get().createStack()), locale));
        }
        audience.ifPresent(it -> {
            for (Component component : components) {
                it.sendMessage(component);
            }
        });
    }
}
