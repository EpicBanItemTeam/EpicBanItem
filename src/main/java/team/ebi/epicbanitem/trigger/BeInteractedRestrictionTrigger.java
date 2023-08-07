/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.trigger;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;

import net.kyori.adventure.audience.Audience;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.util.ItemUtils;

public class BeInteractedRestrictionTrigger extends EBIRestrictionTrigger {
    public BeInteractedRestrictionTrigger() {
        super(EpicBanItem.key("be_interacted"));
    }

    @Listener
    @Include({InteractBlockEvent.Primary.Start.class, InteractBlockEvent.Secondary.class})
    public void onInteractBlock(InteractBlockEvent event, @Getter("block") BlockSnapshot block) {
        // Will trigger on both hands
        final var cause = event.cause();
        final var location = block.location().get();
        final var processed = this.processBlockCancellable(
                event,
                block.location().map(ServerLocation::world).get(),
                cause.first(Subject.class).orElse(null),
                cause.first(Audience.class).orElse(null),
                block);
        final var processedBlock = processed.flatMap(it -> ItemUtils.toBlock(it, location, block.state()));
        if (processed.isPresent())
            if (processedBlock.isPresent()) {
                processedBlock.get().restore(true, BlockChangeFlags.DEFAULT_PLACEMENT);
            } else {
                BlockSnapshot.builder()
                        .from(location)
                        .blockState(
                                BlockState.builder().blockType(BlockTypes.AIR).build())
                        .build()
                        .restore(true, BlockChangeFlags.DEFAULT_PLACEMENT);
                location.spawnEntity(ItemUtils.droppedItem(processed.get(), location));
            }
    }
}
