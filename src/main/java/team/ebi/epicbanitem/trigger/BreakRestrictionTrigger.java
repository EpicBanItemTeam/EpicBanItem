/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.ItemUtils;

public class BreakRestrictionTrigger extends EBIRestrictionTrigger {
    @Inject
    private PluginContainer plugin;

    public BreakRestrictionTrigger() {
        super(EpicBanItem.key("break"));
    }

    @Listener
    public void onChangeBlock(ChangeBlockEvent.All event, @Getter("world") ServerWorld world) {
        final var cause = event.cause();
        final var audience = cause.first(Audience.class).orElse(null);
        final var subject = cause.first(Subject.class).orElse(null);
        event.transactions(Operations.BREAK.get()).forEach(transaction -> {
            final var original = transaction.original();
            final var location = original.location().orElseThrow();
            final var processed = this.processBlockCancellable(
                    event, world, subject, audience, original, ignored -> transaction.invalidate());
            if (processed.isPresent()) {
                final var processedItem = processed.get();
                final var blockType = processedItem.type().block();
                blockType
                        .flatMap(ignored -> ItemUtils.toBlock(processedItem, location, original.state()))
                        .ifPresentOrElse(
                                it -> Sponge.server()
                                        .scheduler()
                                        .submit(Task.builder()
                                                .plugin(plugin)
                                                .execute(() -> it.restore(true, BlockChangeFlags.DEFAULT_PLACEMENT))
                                                .build()),
                                () -> {
                                    final var item = location.createEntity(EntityTypes.ITEM.get());
                                    item.offer(Value.mutableOf(Keys.ITEM_STACK_SNAPSHOT, processedItem));
                                    item.offer(Value.mutableOf(Keys.PICKUP_DELAY, Ticks.of(40L)));
                                    location.spawnEntity(item);
                                });
            }
        });
    }
}
