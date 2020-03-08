package team.ebi.epicbanitem.handler;

import static team.ebi.epicbanitem.util.NbtTagDataUtil.toItemStack;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import team.ebi.epicbanitem.api.CheckResult;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.util.TextUtil;

/**
 * @author The EpicBanItem Team
 */

@Singleton
public class DropHandler {
    @Inject
    private CheckRuleService service;

    @Inject
    public DropHandler(PluginContainer pluginContainer, EventManager eventManager) {
        eventManager.registerListeners(pluginContainer, this);
    }

    @Include({ DropItemEvent.Destruct.class, DropItemEvent.Dispense.class, DropItemEvent.Custom.class })
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDrop(SpawnEntityEvent event, @First Player player, @Getter("getEntities") List<Entity> entities) {
        entities.removeIf(
            entity -> {
                if (entity instanceof Item) {
                    ItemStackSnapshot item = ((Item) entity).item().get();
                    CheckResult result = service.check(item, player.getWorld(), Triggers.DROP, player);
                    if (result.isBanned()) {
                        Optional<ItemStack> optionalItemStack = result.getFinalView().map(view -> toItemStack(view, item.getQuantity()));

                        Text originItemName = TextUtil.getDisplayName(item);
                        Text finalItemName = TextUtil.getDisplayName(optionalItemStack.orElse(item.createStack()));
                        TextUtil
                            .prepareMessage(
                                Triggers.DROP,
                                originItemName,
                                finalItemName,
                                ((CheckResult.Banned) result).getBanRules(),
                                result.isUpdateNeeded()
                            )
                            .forEach(player::sendMessage);

                        if (optionalItemStack.isPresent()) {
                            entity.offer(Keys.REPRESENTED_ITEM, optionalItemStack.get().createSnapshot());
                        } else {
                            event.setCancelled(true);
                            return true;
                        }
                    }
                }
                return false;
            }
        );
    }
}
