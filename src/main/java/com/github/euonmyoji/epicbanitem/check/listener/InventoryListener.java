package com.github.euonmyoji.epicbanitem.check.listener;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.*;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.List;
import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class InventoryListener {

    private CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onThrown(ClickInventoryEvent.Drop event, @First Player player) {
        for (SlotTransaction transaction : event.getTransactions()) {
            ItemStackSnapshot item = transaction.getOriginal();
            CheckResult result = service.check(item, player.getWorld(), Triggers.THROW, player);
            if (result.isBanned()) {
                event.setCancelled(true);
                result.getFinalView().ifPresent(view -> {
                    ItemStack stack = NbtTagDataUtil.toItemStack(view, item.getQuantity());
                    transaction.setCustom(stack.createSnapshot());
                });
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDropped(DropItemEvent.Pre event, @First Entity entity) {
        List<ItemStackSnapshot> droppedItems = event.getDroppedItems();
        Player player = entity instanceof Player ? (Player) entity : null;
        for (int i = droppedItems.size() - 1; i >= 0; --i) {
            ItemStackSnapshot item = droppedItems.get(i);
            CheckResult result = service.check(item, entity.getWorld(), Triggers.DROP, player);
            if (result.isBanned()) {
                int immutableIndex = i;
                result.getFinalView().ifPresent(view -> {
                    ItemStack stack = NbtTagDataUtil.toItemStack(view, item.getQuantity());
                    droppedItems.set(immutableIndex, stack.createSnapshot());
                });
            }
        }
    }


    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onCrafting(AffectItemStackEvent event) {
        if (EpicBanItem.getSettings().isCraftingEventClass(event)) {
            Cause cause = event.getCause();
            Optional<Player> playerOptional = cause.first(Player.class);
            World world = cause.first(Locatable.class).map(Locatable::getWorld).orElseGet(() -> {
                RuntimeException e = new RuntimeException();
                WorldProperties defProps = Sponge.getServer().getDefaultWorld().orElseThrow(RuntimeException::new);
                World def = Sponge.getServer().getWorld(defProps.getUniqueId()).orElseThrow(RuntimeException::new);
                EpicBanItem.getLogger().error("EpicBanItem cannot even find a world! What is the server doing?", e);
                return def;
            });
            for (Transaction<ItemStackSnapshot> transaction : event.getTransactions()) {
                ItemStackSnapshot item = transaction.getFinal();
                CheckResult result = service.check(item, world, Triggers.CRAFT, playerOptional.orElse(null));
                if (result.isBanned()) {
                    result.getFinalView().ifPresent(view -> {
                        ItemStack stack = NbtTagDataUtil.toItemStack(view, item.getQuantity());
                        transaction.setCustom(stack.createSnapshot());
                    });
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @Exclude({ClickInventoryEvent.Drop.class})
    public void onClicked(ClickInventoryEvent event, @First Player player) {
        Transaction<ItemStackSnapshot> transaction = event.getCursorTransaction();
        ItemStackSnapshot item = transaction.getFinal();
        String trigger = Triggers.CLICK;
        CheckResult result = service.check(item, player.getWorld(), trigger, player);
        if (result.isBanned()) {
            Optional<DataContainer> viewOptional = result.getFinalView();
            if (viewOptional.isPresent()) {
                ItemStack stack = NbtTagDataUtil.toItemStack(viewOptional.get(), item.getQuantity());
                transaction.setCustom(stack.createSnapshot());
            } else {
                event.setCancelled(true);
                return; // Event cancelled, so there is no need to check slots.
            }
        }
        onInventoryChanged(event, player, trigger);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPickedUp(ChangeInventoryEvent.Pickup.Pre event, @First Player player) {
        Item itemEntity = event.getTargetEntity();
        ItemStackSnapshot item = itemEntity.getItemData().item().get();
        CheckResult result = service.check(item, player.getWorld(), Triggers.PICKUP, player);
        if (result.isBanned()) {
            event.setCancelled(true);
            result.getFinalView().ifPresent(view -> {
                ItemStack stack = NbtTagDataUtil.toItemStack(view, item.getQuantity());
                itemEntity.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
            });
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @Include(HandInteractEvent.class)
    public void onItemUsed(InteractItemEvent event, @First Player player) {
        ItemStack item = event.getItemStack().createStack();
        CheckResult result = service.check(item, player.getWorld(), Triggers.USE, player);
        if (result.isBanned()) {
            event.setCancelled(true);
            result.getFinalView().ifPresent(view -> {
                ItemStack stack = NbtTagDataUtil.toItemStack(view, item.getQuantity());
                player.setItemInHand(((HandInteractEvent) event).getHandType(), stack);
            });
        }
    }

    private void onInventoryChanged(ChangeInventoryEvent event, Player player, String trigger) {
        for (SlotTransaction slotTransaction : event.getTransactions()) {
            ItemStackSnapshot item = slotTransaction.getFinal();
            CheckResult result = service.check(item, player.getWorld(), trigger, player);
            if (result.isBanned()) {
                Optional<DataContainer> viewOptional = result.getFinalView();
                if (viewOptional.isPresent()) {
                    ItemStack stack = NbtTagDataUtil.toItemStack(viewOptional.get(), item.getQuantity());
                    slotTransaction.setCustom(stack);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
}
