package team.ebi.epicbanitem.check.listener;

import static team.ebi.epicbanitem.util.NbtTagDataUtil.toItemStack;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.Transaction;
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
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent.Drop;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
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
public class ThrowListener {
    @Inject
    private CheckRuleService service;

    @Inject
    public ThrowListener(PluginContainer pluginContainer, EventManager eventManager) {
        eventManager.registerListeners(pluginContainer, this);
    }

    @Include({ Drop.class, DropItemEvent.Close.class })
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onThrow(SpawnEntityEvent event, @First Player player, @Getter("getEntities") List<Entity> entities) {
        entities.removeIf(
            entity -> {
                if (entity instanceof Item) {
                    ItemStackSnapshot item = ((Item) entity).item().get();
                    CheckResult result = service.check(item, player.getWorld(), Triggers.THROW, player);
                    if (result.isBanned()) {
                        Optional<ItemStack> optionalItemStack = result.getFinalView().map(view -> toItemStack(view, item.getQuantity()));

                        Text originItemName = TextUtil.getDisplayName(item);
                        Text finalItemName = TextUtil.getDisplayName(optionalItemStack.orElse(item.createStack()));
                        TextUtil
                            .prepareMessage(
                                Triggers.THROW,
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

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onCloseThrown(
        InteractInventoryEvent.Close event,
        @First Player player,
        @Getter("getCursorTransaction") Transaction<ItemStackSnapshot> transaction
    ) {
        ItemStackSnapshot item = transaction.getOriginal();
        CheckResult result = service.check(item, player.getWorld(), Triggers.THROW, player);
        if (result.isBanned()) {
            Optional<ItemStack> optionalFinalItem = result.getFinalView().map(view -> toItemStack(view, item.getQuantity()));
            ItemStack itemStack = item.createStack();
            ItemStack newItem = optionalFinalItem.orElse(itemStack);
            if (!optionalFinalItem.isPresent()) {
                player.getInventory().offer(itemStack);
            }
        }
    }
}
