package com.github.euonmyoji.epicbanitem.check.listener;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class ChunkListener {

    private final Server server = Sponge.getServer();
    private final CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeBlockPre(ChangeBlockEvent.Pre event, @First Player player) {
        List<Location<World>> locations = event.getLocations();
        List<BlockSnapshot> scheduledChanges = new ArrayList<>(locations.size());
        for (Location<World> location : locations) {
            BlockSnapshot snapshot = location.createSnapshot();
            CheckResult result = service.check(snapshot, location.getExtent(), Triggers.BREAK, player);
            if (result.isBanned()) {
                event.setCancelled(true);
                result.getFinalView().ifPresent(view -> {
                    UUID worldUniqueId = snapshot.getWorldUniqueId();
                    scheduledChanges.add(NbtTagDataUtil.toBlockSnapshot(view, worldUniqueId));
                });
            }
        }
        for (BlockSnapshot scheduledChange : scheduledChanges) {
            scheduledChange.restore(true, BlockChangeFlags.NONE);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(ChangeBlockEvent.Post event, @First Player player) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            boolean isValidBefore = transaction.isValid();
            BlockSnapshot snapshot = transaction.getFinal();
            UUID worldUniqueId = snapshot.getWorldUniqueId();
            World world = server.getWorld(worldUniqueId).orElse(player.getWorld());
            CheckResult result = service.check(snapshot, world, Triggers.PLACE, player);
            if (result.isBanned()) {
                transaction.setValid(false);
                result.getFinalView().ifPresent(view -> {
                    transaction.setValid(isValidBefore);
                    transaction.setCustom(NbtTagDataUtil.toBlockSnapshot(view, worldUniqueId));
                });
            }
        }
    }
}
