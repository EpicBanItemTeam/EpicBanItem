package com.github.euonmyoji.epicbanitem.listener;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;

public class GetItemListener {
    @Listener
    public void onChangeInv(ChangeInventoryEvent event) {
        if(event instanceof ChangeInventoryEvent.Pickup){

        }
    }
}
