package com.github.euonmyoji.epicbanitem.listener;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import org.spongepowered.api.Sponge;
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

/**
 * @author yinyangshi & dalaos
 */
public class InventoryListener {
    private CheckRuleService service = Sponge.getServiceManager().provide(CheckRuleService.class)
            .orElseThrow(() -> new ProviderNotFoundException("No CheckRuleService found!"));

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDrop(ClickInventoryEvent.Drop event, @First Player player) {
        onChangeInv(event, player, "drop");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @Exclude({ClickInventoryEvent.Drop.class, ClickInventoryEvent.Creative.class})
    public void onClick(ClickInventoryEvent event, @First Player player) {
        onChangeInv(event, player, "click");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPickUp(ChangeInventoryEvent.Pickup.Pre event, @First Player player) {
        ItemStackSnapshot item = event.getOriginalStack();
        CheckResult result;
        if ((result = service.check(item, player.getWorld(), "pickup", player)).isBanned()) {
            event.setCancelled(true);
            if (result.shouldRemove()) {
                event.setCustom(Collections.emptyList());
            } else if (result.getFinalView().isPresent()) {
                event.setCustom(Collections.singletonList(
                        NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity()).createSnapshot()
                ));
            }
        }
    }

    private void onChangeInv(ChangeInventoryEvent event, Player player, String trigger) {
        for (SlotTransaction slotTransaction : event.getTransactions()) {
            ItemStackSnapshot item = slotTransaction.getOriginal();
            CheckResult result;
            if ((result = service.check(item, player.getWorld(), trigger, player)).isBanned()) {
                event.setCancelled(true);
                if (result.shouldRemove()) {
                    slotTransaction.setCustom(ItemStack.empty());
                } else if (result.getFinalView().isPresent()) {
                    slotTransaction.setCustom(NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity()));
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onUseItem(InteractItemEvent event, @First Player player) {
        ItemStack item = event.getItemStack().createStack();
        CheckResult result;
        if ((result = service.check(item, player.getWorld(), "use", player)).isBanned()) {
            event.setCancelled(true);
            ItemStack newItemStack;
            if (result.shouldRemove()) {
                newItemStack = ItemStack.empty();
            } else if (result.getFinalView().isPresent()) {
                newItemStack = NbtTagDataUtil.toItemStack(result.getFinalView().get(), item.getQuantity());
            } else {
                return;
            }

            //应该只可能有这一种情况 所以没有else
            if (event instanceof HandInteractEvent) {
                HandType handType = ((HandInteractEvent) event).getHandType();
                player.setItemInHand(handType, newItemStack);
            }
        }
    }
}