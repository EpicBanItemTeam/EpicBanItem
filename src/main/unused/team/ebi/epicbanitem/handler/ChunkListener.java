package team.ebi.epicbanitem.handler;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.TextUtil;

/**
 * @author The EpicBanItem Team
 */
@Singleton
public class ChunkListener {
    private Server server;

    @Inject
    private CheckRuleService service;

    @Inject
    public ChunkListener(PluginContainer pluginContainer, EventManager eventManager) {
        eventManager.registerListeners(pluginContainer, this);
        this.server = Sponge.getServer();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChangeBlockPre(ChangeBlockEvent.Pre event, @First Player player) {
        List<Location<World>> locations = event.getLocations();
        List<BlockSnapshot> scheduledChanges = Lists.newArrayListWithExpectedSize(locations.size());
        for (Location<World> location : locations) {
            BlockSnapshot snapshot = location.createSnapshot();
            CheckResult result = service.check(snapshot, location.getExtent(), Triggers.BREAK, player);
            if (result.isBanned()) {
                event.setCancelled(true);
                Optional<BlockSnapshot> optionalFinal = result
                    .getFinalView()
                    .map(
                        view -> {
                            UUID worldUniqueId = snapshot.getWorldUniqueId();
                            return NbtTagDataUtil.toBlockSnapshot(view, worldUniqueId);
                        }
                    );
                optionalFinal.ifPresent(scheduledChanges::add);
                Text originBlockName = Text.of(snapshot.getState().getType().getTranslation());
                Text finalBlockName = Text.of(optionalFinal.orElse(snapshot).getState().getType().getTranslation());
                TextUtil
                    .prepareMessage(
                        Triggers.BREAK,
                        originBlockName,
                        finalBlockName,
                        ((CheckResult.Banned) result).getBanRules(),
                        result.isUpdateNeeded()
                    )
                    .forEach(player::sendMessage);
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
                Optional<BlockSnapshot> optionalFinal = result.getFinalView().map(view -> NbtTagDataUtil.toBlockSnapshot(view, worldUniqueId));
                optionalFinal.ifPresent(
                    blockSnapshot -> {
                        transaction.setValid(isValidBefore);
                        transaction.setCustom(blockSnapshot);
                    }
                );
                Text originBlockName = Text.of(snapshot.getState().getType().getTranslation());
                Text finalBlockName = Text.of(optionalFinal.orElse(snapshot).getState().getType().getTranslation());
                TextUtil
                    .prepareMessage(
                        Triggers.PLACE,
                        originBlockName,
                        finalBlockName,
                        ((CheckResult.Banned) result).getBanRules(),
                        result.isUpdateNeeded()
                    )
                    .forEach(player::sendMessage);
            }
        }
    }
}
