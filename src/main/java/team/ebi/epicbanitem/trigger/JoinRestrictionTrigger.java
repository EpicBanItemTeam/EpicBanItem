/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.trigger.AbstractRestrictionTrigger;
import team.ebi.epicbanitem.util.EventUtils;

public class JoinRestrictionTrigger extends AbstractRestrictionTrigger {
    @Inject
    private PluginContainer plugin;

    public JoinRestrictionTrigger() {
        super(EpicBanItem.key("join"));
    }

    @Listener
    public void onSpawnEntity(SpawnEntityEvent.Pre event) {
        final var cause = event.cause();
        final var audience = cause.last(Audience.class).orElse(null);
        final var locale = EventUtils.locale(cause);
        event.entities().stream()
                .filter(Carrier.class::isInstance)
                .map(it -> ((Carrier) it).inventory())
                .forEach(it -> handleInventory(it, event, audience, locale));
    }

    @Listener
    public void onServerSideConnectionJoin(ServerSideConnectionEvent.Join event) {
        final var player = event.player();
        final var locale = player.locale();
        Sponge.server()
                .scheduler()
                .submit(Task.builder()
                        .plugin(plugin)
                        .delay(Ticks.of(1L))
                        .execute(() -> handleInventory(player.inventory(), event, player, locale))
                        .build());
    }
}
