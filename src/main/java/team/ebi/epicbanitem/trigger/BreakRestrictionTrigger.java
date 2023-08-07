/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.Subject;
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
            final var location = original.location().get();
            final var processed = this.processBlockCancellable(
                    event, world, subject, audience, original, ignored -> transaction.invalidate());
            if (processed.isPresent()) {
                final var processedItem = processed.get();
                final var blockType = processedItem.type().block();
                final var processedBlock =
                        blockType.flatMap(ignored -> ItemUtils.toBlock(processedItem, location, original.state()));
                if (processedBlock.isPresent()) {
                    Sponge.server()
                            .scheduler()
                            .submit(Task.builder()
                                    .plugin(plugin)
                                    .execute(() ->
                                            processedBlock.get().restore(true, BlockChangeFlags.DEFAULT_PLACEMENT))
                                    .build());
                } else {
                    location.spawnEntity(ItemUtils.droppedItem(processedItem, location));
                }
            }
        });
    }
}
