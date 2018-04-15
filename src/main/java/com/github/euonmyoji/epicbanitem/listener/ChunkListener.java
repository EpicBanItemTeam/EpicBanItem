package com.github.euonmyoji.epicbanitem.listener;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.GenerateChunkEvent;
import org.spongepowered.api.scheduler.Task;

/**
 * @author yinyangshi
 */
public class ChunkListener {
    @Listener
    public void onGenerateChunk(GenerateChunkEvent event) {
        Task.builder().async().execute(() -> {
        }).name("EpicBanItem - Test the new chunk:" + event.getTargetChunk().getUniqueId()).submit(EpicBanItem.plugin);
    }
}
