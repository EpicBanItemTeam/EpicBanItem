/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.registry.RegistryTypes;

import com.google.common.collect.Lists;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.translation.GlobalTranslator;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.SingleTargetRestrictionTrigger;

public class PlaceRestrictionTrigger extends SingleTargetRestrictionTrigger {
    public PlaceRestrictionTrigger() {
        super(EpicBanItem.key("place"));
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.All event) {
        final var audience = event.cause().last(Audience.class);
        final var locale = locale(event.cause());
        event.transactions(Operations.PLACE.get()).forEach(transaction -> {
            final var components = Lists.<Component>newArrayList();
            final var block = transaction.finalReplacement();
            final var type = block.state().type();
            this.process(
                            event,
                            block.toContainer(),
                            type.key(RegistryTypes.BLOCK_TYPE),
                            rule -> components.add(GlobalTranslator.render(
                                    rule.canceledMessage().args(rule, this, type), locale)),
                            (rule, result) -> {
                                if (result.isEmpty()) return;
                                components.add(GlobalTranslator.render(
                                        rule.updatedMessage()
                                                .args(
                                                        rule,
                                                        this,
                                                        type,
                                                        result.get().state().type()),
                                        locale));
                            },
                            view -> Sponge.dataManager().deserialize(BlockSnapshot.class, view))
                    .ifPresent(transaction::setCustom);
            audience.ifPresent(it -> it.sendMessage(Component.join(JoinConfiguration.newlines(), components)));
        });
    }
}
