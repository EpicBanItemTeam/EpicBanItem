package com.github.euonmyoji.epicbanitem.listener;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

public class GetItemListener {

    @Listener
    public void onChangeInv(ChangeInventoryEvent event) {

    }

    @Listener
    public void onClickInv(ClickInventoryEvent event) {
        if (event instanceof ClickInventoryEvent.Drag || event instanceof ClickInventoryEvent.Shift) {
            for (SlotTransaction slotTransaction : event.getTransactions()) {
                ItemStackSnapshot d = slotTransaction.getDefault();
                ItemStackSnapshot o = slotTransaction.getOriginal();
                ItemStackSnapshot f = slotTransaction.getFinal();
            }
        }
    }
}
