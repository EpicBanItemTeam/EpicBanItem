package com.github.euonmyoji.epicbanitem.check.listener;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
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
 * @author yinyangshi
 */
public class ChunkListener {

    private CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeBlockPre(ChangeBlockEvent.Pre event, @First Player player) {
        List<Location<World>> locations = event.getLocations();
        List<BlockSnapshot> scheduledChanges = new ArrayList<>(locations.size());
        for (Location<World> location : locations) {
            BlockSnapshot snapshot = location.createSnapshot();
            // noinspection unused
            String trigger = Triggers.BREAK;
            CheckResult result = CheckResult.empty(); // TODO: add methods for checking blocks in CheckRuleService
            if (result.isBanned()) {
                event.setCancelled(true);
                result.getFinalView().ifPresent(view -> {
                    BlockState oldState = snapshot.getState();
                    UUID worldUniqueId = snapshot.getWorldUniqueId();
                    scheduledChanges.add(NbtTagDataUtil.toBlockSnapshot(view, oldState, worldUniqueId));
                });
            }
        }
        for (BlockSnapshot scheduledChange : scheduledChanges) {
            scheduledChange.restore(true, BlockChangeFlags.NONE);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeBlockPost(ChangeBlockEvent.Post event, @First Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> transaction : transactions) {
            BlockSnapshot snapshot = transaction.getFinal();
            // noinspection unused
            String trigger = Triggers.PLACE;
            CheckResult result = CheckResult.empty(); // TODO: add methods for checking blocks in CheckRuleService
            if (result.isBanned()) {
                result.getFinalView().ifPresent(view -> {
                    BlockState oldState = snapshot.getState();
                    UUID worldUniqueId = snapshot.getWorldUniqueId();
                    transaction.setCustom(NbtTagDataUtil.toBlockSnapshot(view, oldState, worldUniqueId));
                });
            }
        }
    }
}
