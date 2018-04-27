package com.github.euonmyoji.epicbanitem.listener;

import com.github.euonmyoji.epicbanitem.check.CheckResult;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.nio.file.ProviderNotFoundException;

/**
 * @author yinyangshi & dalaos
 */
public class GetItemListener {

    @Listener
    public void onChangeInv(ChangeInventoryEvent event) {
        //判断trigger 可能有点乱:D
        String trigger = event instanceof ClickInventoryEvent.Drag
                || event instanceof ClickInventoryEvent.Shift ?
                "click" : event instanceof ChangeInventoryEvent.Pickup ?
                "pickup" : event instanceof ChangeInventoryEvent.Transfer ?
                "transfer" : null;
        if (trigger != null) {
            CheckRuleService service = Sponge.getServiceManager().provide(CheckRuleService.class)
                    .orElseThrow(() -> new ProviderNotFoundException("No CheckRuleService found!"));
            Player p = event.getCause().first(Player.class).orElseThrow(NoSuchFieldError::new);
            for (SlotTransaction slotTransaction : event.getTransactions()) {
                ItemStack item = slotTransaction.getOriginal().createStack();
                CheckResult result;
                while ((result = service.check(item, p, trigger)).isBanned()) {
                    event.setCancelled(true);
                    if (result.shouldRemove()) {
                        slotTransaction.setCustom(ItemStack.empty());
                        return;
                        //如果要remove 告辞
                    }
                    result.getFinalView().ifPresent(item::setRawData);
                }
                slotTransaction.setCustom(item);
            }
        }
    }
}
