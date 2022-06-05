/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.slot.FilteringSlot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.InventoryUtils;

public class JoinRestrictionTrigger extends EBIRestrictionTrigger {
    @Inject
    private PluginContainer plugin;

    public JoinRestrictionTrigger() {
        super(EpicBanItem.key("join"));
    }

    @Listener
    public void onSpawnEntity(
            final SpawnEntityEvent.Pre event, final @First ServerWorld world, final @First Locatable locatable) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        final var location = locatable.serverLocation();
        event.entities().stream()
                .filter(Carrier.class::isInstance)
                .map(it -> ((Carrier) it).inventory())
                .forEach(inventory -> handleInventory(event, world, audience, subject, location, inventory));
    }

    @Listener
    public void onServerSideConnectionJoin(final ServerSideConnectionEvent.Join event) {
        final var player = event.player();
        final var world = player.world();
        final var location = player.serverLocation();
        final var inventory = player.inventory();
        Sponge.server()
                .scheduler()
                .submit(Task.builder()
                        .plugin(plugin)
                        .delay(Ticks.of(1L))
                        .execute(() -> handleInventory(event, world, player, player, location, inventory))
                        .build());
    }

    private void handleInventory(
            final Event event,
            final ServerWorld world,
            final Audience audience,
            final Subject subject,
            final ServerLocation location,
            final CarriedInventory<? extends Carrier> inventory) {
        inventory.slots().stream().filter(it -> it.freeCapacity() == 0).forEach(it -> this.processItem(
                        event, world, subject, audience, it.peek().createSnapshot())
                .map(ItemStackSnapshot::createStack)
                .ifPresent(item -> {
                    if (it instanceof FilteringSlot slot && slot.isValidItem(item))
                        location.spawnEntities(InventoryUtils.offerOrDrop(inventory, location, item));
                    else it.set(item);
                }));
    }
}
