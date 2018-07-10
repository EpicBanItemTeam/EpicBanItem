package com.github.euonmyoji.epicbanitem.listener;

import com.github.euonmyoji.epicbanitem.Triggers;
import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.nio.file.ProviderNotFoundException;
import java.util.Collections;

//todo:ClickInventoryEvent.Creative
//todo:Remove -> cancel the event then remove;

/**
 * @author yinyangshi & dalaos
 */
public class InventoryListener {
    private CheckRuleService service = Sponge.getServiceManager().provide(CheckRuleService.class)
            .orElseThrow(() -> new ProviderNotFoundException("No CheckRuleService found!"));

    //todo:press q throw

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDrop(ClickInventoryEvent.Drop event, @First Player player) {
        onChangeInv(event, player, "drop");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @Exclude({ClickInventoryEvent.Drop.class, ClickInventoryEvent.Creative.class})
    public void onClick(ClickInventoryEvent event, @First Player player) {
        Transaction<ItemStackSnapshot> transaction = event.getCursorTransaction();
        ItemStackSnapshot item = transaction.getFinal();
        CheckResult result;
        if ((result = service.check(item, player.getWorld(), Triggers.CLICK, player)).isBanned()) {
            if (result.shouldRemove()) {
                transaction.setCustom(ItemStackSnapshot.NONE);
            } else if (result.getFinalView().isPresent()) {
                transaction.setCustom(NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity()).createSnapshot());
            } else {
                event.setCancelled(true);
                //Event cancelled, so there is no need to check slots.
                return;
            }
        }
        onChangeInv(event, player, Triggers.CLICK);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPickUp(ChangeInventoryEvent.Pickup.Pre event, @First Player player) {
        ItemStackSnapshot item = event.getOriginalStack();
        CheckResult result;
        if ((result = service.check(item, player.getWorld(), Triggers.PICKUP, player)).isBanned()) {
            if (result.shouldRemove()) {
                event.setCustom(Collections.emptyList());
            } else if (result.getFinalView().isPresent()) {
                event.setCustom(Collections.singletonList(
                        NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity()).createSnapshot()
                ));
            } else {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onUseItem(InteractItemEvent event, @First Player player) {
        ItemStack item = event.getItemStack().createStack();
        CheckResult result;
        if ((result = service.check(item, player.getWorld(), Triggers.USE, player)).isBanned()) {
            event.setCancelled(true);
            ItemStack newItemStack;
            if (result.shouldRemove()) {
                newItemStack = ItemStack.empty();
            } else if (result.getFinalView().isPresent()) {
                newItemStack = NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity());
            } else {
                return;
            }

            if (event instanceof HandInteractEvent) {
                HandType handType = ((HandInteractEvent) event).getHandType();
                player.setItemInHand(handType, newItemStack);
            } else {
                throw new IllegalStateException("There is Something Wrong With the Universe");
            }
        }
    }

    private void onChangeInv(ChangeInventoryEvent event, Player player, String trigger) {
        for (SlotTransaction slotTransaction : event.getTransactions()) {
            ItemStackSnapshot item = slotTransaction.getFinal();
            CheckResult result;
            if ((result = service.check(item, player.getWorld(), trigger, player)).isBanned()) {
                if (result.shouldRemove()) {
                    slotTransaction.setCustom(ItemStack.empty());
                } else if (result.getFinalView().isPresent()) {
                    slotTransaction.setCustom(NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity()));
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
}
