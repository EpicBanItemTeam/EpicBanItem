/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;

public class JoinRestrictionTrigger extends EBIRestrictionTrigger {
    @Inject
    private PluginContainer plugin;

    public JoinRestrictionTrigger() {
        super(EpicBanItem.key("join"));
    }

    @Listener
    public void onSpawnEntity(SpawnEntityEvent.Pre event, @Last ServerWorld world) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        event.entities().stream()
                .filter(Carrier.class::isInstance)
                .map(it -> ((Carrier) it).inventory())
                .flatMap(it -> it.slots().stream())
                .filter(it -> it.freeCapacity() == 0)
                .forEach(it -> this.process(
                                event, world, subject, audience, it.peek().createSnapshot())
                        .map(ItemStackSnapshot::createStack)
                        .ifPresent(it::set));
    }

    @Listener
    public void onServerSideConnectionJoin(ServerSideConnectionEvent.Join event) {
        final var player = event.player();
        final var world = player.world();
        Sponge.server()
                .scheduler()
                .submit(Task.builder()
                        .plugin(plugin)
                        .delay(Ticks.of(1L))
                        .execute(() -> player.inventory().slots().stream()
                                .filter(it -> it.freeCapacity() == 0)
                                .forEach(it -> this.process(
                                                event,
                                                world,
                                                player,
                                                player,
                                                it.peek().createSnapshot())
                                        .map(ItemStackSnapshot::createStack)
                                        .ifPresent(it::set)))
                        .build());
    }
}
