package com.github.euonmyoji.epicbanitem.check.listener;

import static com.github.euonmyoji.epicbanitem.util.TextUtil.getDisplayName;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.api.CheckResult;
import com.github.euonmyoji.epicbanitem.api.CheckRuleTrigger;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.*;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.equipment.WornEquipmentType;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@SuppressWarnings("DuplicatedCode")
public class InventoryListener {
    private CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);

    private static ItemStack getItem(DataContainer view, int quantity) {
        return NbtTagDataUtil.toItemStack(view, quantity);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onThrown(ClickInventoryEvent.Drop event, @First Player player) {
        for (SlotTransaction tran : event.getTransactions()) {
            ItemStackSnapshot item = tran.getOriginal();
            CheckResult result = service.check(item, player.getWorld(), Triggers.THROW, player);
            if (result.isBanned()) {
                event.setCancelled(true);
                Optional<ItemStack> optionalFinalItem = result.getFinalView().map(view -> getItem(view, item.getQuantity()));
                optionalFinalItem.ifPresent(finalItem -> tran.setCustom(finalItem.createSnapshot()));
                Text originItemName = getDisplayName(item.createStack());
                Text finalItemName = getDisplayName(optionalFinalItem.orElse(item.createStack()));
                TextUtil
                    .prepareMessage(
                        Triggers.THROW,
                        originItemName,
                        finalItemName,
                        ((CheckResult.Banned) result).getBanRules(),
                        result.isUpdateNeeded()
                    )
                    .forEach(player::sendMessage);
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
                Optional<ItemStack> optionalFinalItem = result.getFinalView().map(view -> getItem(view, item.getQuantity()));
                optionalFinalItem.ifPresent(finalItem -> droppedItems.set(immutableIndex, finalItem.createSnapshot()));
                if (player != null) {
                    Text originItemName = getDisplayName(item.createStack());
                    Text finalItemName = getDisplayName(optionalFinalItem.orElse(item.createStack()));
                    TextUtil
                        .prepareMessage(
                            Triggers.DROP,
                            originItemName,
                            finalItemName,
                            ((CheckResult.Banned) result).getBanRules(),
                            result.isUpdateNeeded()
                        )
                        .forEach(player::sendMessage);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onCrafting(AffectItemStackEvent event) {
        if (EpicBanItem.getSettings().isCraftingEventClass(event)) {
            Cause cause = event.getCause();
            List<World> worlds = new ArrayList<>();
            Optional<Player> playerOptional = cause.first(Player.class);
            playerOptional.ifPresent(player -> worlds.add(player.getWorld()));
            if (worlds.isEmpty()) {
                if (event instanceof TargetInventoryEvent) {
                    Inventory inventory = ((TargetInventoryEvent) event).getTargetInventory();
                    if (inventory instanceof Container) {
                        ((Container) inventory).getViewers().forEach(player -> worlds.add(player.getWorld()));
                    }
                }
            }
            if (worlds.isEmpty()) {
                worlds.add(
                    cause
                        .first(Locatable.class)
                        .map(Locatable::getWorld)
                        .orElseGet(
                            () -> {
                                RuntimeException e = new RuntimeException("EpicBanItem cannot even find a world when crafting");
                                WorldProperties defProps = Sponge.getServer().getDefaultWorld().orElseThrow(RuntimeException::new);
                                World def = Sponge.getServer().getWorld(defProps.getUniqueId()).orElseThrow(RuntimeException::new);
                                EpicBanItem.getLogger().warn("EpicBanItem cannot even find a world! What is the server doing?");
                                EpicBanItem.getLogger().debug("No world found in " + cause.toString(), e);
                                return def;
                            }
                        )
                );
            }
            for (Transaction<ItemStackSnapshot> transaction : event.getTransactions()) {
                ItemStackSnapshot item = transaction.getFinal();
                for (World world : worlds) {
                    CheckResult result = service.check(item, world, Triggers.CRAFT, playerOptional.orElse(null));
                    if (result.isBanned()) {
                        Optional<DataContainer> viewOptional = result.getFinalView();
                        if (viewOptional.isPresent()) {
                            item = getItem(viewOptional.get(), item.getQuantity()).createSnapshot();
                        }
                    }
                }
                transaction.setCustom(item);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @Exclude({ ClickInventoryEvent.Drop.class })
    public void onClicked(ClickInventoryEvent event, @First Player player) {
        CheckRuleTrigger trigger = Triggers.CLICK;
        Stream<SlotTransaction> slotTransactionStream = event.getTransactions().stream();
        Stream<Transaction<ItemStackSnapshot>> cursorTransactionStream = Stream.of(event.getCursorTransaction());
        if (this.checkInventory(player, trigger, Stream.concat(cursorTransactionStream, slotTransactionStream))) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPickedUp(ChangeInventoryEvent.Pickup.Pre event, @First Player player) {
        Item itemEntity = event.getTargetEntity();
        ItemStackSnapshot item = itemEntity.getItemData().item().get();
        CheckResult result = service.check(item, player.getWorld(), Triggers.PICKUP, player);
        if (result.isBanned()) {
            event.setCancelled(true);
            Optional<ItemStack> optionalFinalItem = result.getFinalView().map(view -> getItem(view, item.getQuantity()));
            optionalFinalItem.ifPresent(finalItem -> itemEntity.offer(Keys.REPRESENTED_ITEM, finalItem.createSnapshot()));
            Text originItemName = getDisplayName(itemEntity);
            Text finalItemName = getDisplayName(optionalFinalItem.orElse(item.createStack()));
            TextUtil
                .prepareMessage(Triggers.PICKUP, originItemName, finalItemName, ((CheckResult.Banned) result).getBanRules(), result.isUpdateNeeded())
                .forEach(player::sendMessage);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @Include(HandInteractEvent.class)
    public void onItemUsed(InteractItemEvent event, @First Player player) {
        CheckRuleTrigger trigger = Triggers.USE;
        ItemStackSnapshot item = event.getItemStack();
        if (checkUseItem(player, trigger, ((HandInteractEvent) event).getHandType(), item)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEquip(ChangeEntityEquipmentEvent event, @First Player player) {
        Slot slot = event.getTargetInventory();
        Optional<EquipmentSlotType> equipmentSlotType = slot.getInventoryProperty(EquipmentSlotType.class);
        if (equipmentSlotType.isPresent() && equipmentSlotType.get().getValue() instanceof WornEquipmentType) {
            CheckRuleTrigger trigger = Triggers.EQUIP;
            if (this.checkInventory(player, trigger, Stream.of(event.getTransaction()))) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onJoin(Join event, @First Player player) {
        StreamSupport
            .stream(player.getInventory().slots().spliterator(), false)
            .filter(inventory -> inventory instanceof Slot)
            .forEach(
                inventory ->
                    inventory
                        .peek()
                        .ifPresent(
                            itemStack -> {
                                CheckResult checkResult = service.check(itemStack, player.getWorld(), Triggers.JOIN, player);

                                if (checkResult.isBanned()) {
                                    checkResult
                                        .getFinalView()
                                        .map(dataContainer -> NbtTagDataUtil.toItemStack(dataContainer, itemStack.getQuantity()))
                                        .ifPresent(
                                            finalItem -> {
                                                inventory.set(finalItem);
                                                TextUtil
                                                    .prepareMessage(
                                                        Triggers.JOIN,
                                                        TextUtil.getDisplayName(itemStack),
                                                        TextUtil.getDisplayName(finalItem),
                                                        ((CheckResult.Banned) checkResult).getBanRules(),
                                                        checkResult.isUpdateNeeded()
                                                    )
                                                    .forEach(player::sendMessage);
                                            }
                                        );
                                }
                            }
                        )
            );
    }

    @IsCancelled(Tristate.FALSE)
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(InteractBlockEvent event, @First Player player) {
        Optional<ItemStackSnapshot> optionalItem = event.getContext().get(EventContextKeys.USED_ITEM);
        if (optionalItem.isPresent()) {
            CheckRuleTrigger trigger = Triggers.USE;
            ItemStackSnapshot item = optionalItem.get();
            if (checkUseItem(player, trigger, ((HandInteractEvent) event).getHandType(), item)) {
                event.setCancelled(true);
            }
        }
        CheckRuleTrigger trigger = Triggers.INTERACT;
        checkInteractBlock(player, trigger, event.getTargetBlock());
    }

    private void checkInteractBlock(Player player, CheckRuleTrigger trigger, BlockSnapshot snapshot) {
        CheckResult result = service.check(snapshot, player.getWorld(), trigger, player);
        if (result.isBanned()) {
            Optional<BlockSnapshot> optionalFinalBlock = result
                .getFinalView()
                .map(view -> NbtTagDataUtil.toBlockSnapshot(view, snapshot.getWorldUniqueId()));
            optionalFinalBlock.ifPresent(blockSnapshot -> blockSnapshot.restore(true, BlockChangeFlags.NONE));
            Text originName = Text.of(snapshot.getState().getType().getTranslation());
            Text finalName = Text.of(optionalFinalBlock.orElse(snapshot).getState().getType().getTranslation());
            TextUtil
                .prepareMessage(trigger, originName, finalName, ((CheckResult.Banned) result).getBanRules(), result.isUpdateNeeded())
                .forEach(player::sendMessage);
            result
                .getFinalView()
                .ifPresent(
                    view -> {
                        UUID worldUniqueId = snapshot.getWorldUniqueId();
                        NbtTagDataUtil.toBlockSnapshot(view, worldUniqueId).restore(true, BlockChangeFlags.NONE);
                    }
                );
        }
    }

    private boolean checkUseItem(Player player, CheckRuleTrigger trigger, HandType handType, ItemStackSnapshot item) {
        CheckResult result = service.check(item, player.getWorld(), trigger, player);
        if (result.isBanned()) {
            Optional<ItemStack> finalItem = result.getFinalView().map(view -> getItem(view, item.getQuantity()));
            finalItem.ifPresent(itemStack -> player.setItemInHand(handType, itemStack));
            Text originName = getDisplayName(item.createStack());
            Text finalItemName = getDisplayName(finalItem.orElse(item.createStack()));
            TextUtil
                .prepareMessage(trigger, originName, finalItemName, ((CheckResult.Banned) result).getBanRules(), result.isUpdateNeeded())
                .forEach(player::sendMessage);
            return true;
        }
        return false;
    }

    private boolean checkInventory(Player player, CheckRuleTrigger trigger, Stream<? extends Transaction<ItemStackSnapshot>> trans) {
        return trans.anyMatch(
            tran -> {
                ItemStackSnapshot item = tran.getFinal();
                CheckResult result = service.check(item, player.getWorld(), trigger, player);
                if (result.isBanned()) {
                    Optional<DataContainer> viewOptional = result.getFinalView();
                    if (viewOptional.isPresent()) {
                        ItemStack finalItem = getItem(viewOptional.get(), item.getQuantity());
                        TextUtil
                            .prepareMessage(
                                trigger,
                                getDisplayName(item.createStack()),
                                getDisplayName(finalItem),
                                ((CheckResult.Banned) result).getBanRules(),
                                result.isUpdateNeeded()
                            )
                            .forEach(player::sendMessage);
                        tran.setCustom(finalItem.createSnapshot());
                    } else {
                        Text itemName = getDisplayName(item.createStack());
                        TextUtil
                            .prepareMessage(trigger, itemName, itemName, ((CheckResult.Banned) result).getBanRules(), result.isUpdateNeeded())
                            .forEach(player::sendMessage);
                        return true;
                    }
                }
                return false;
            }
        );
    }
}
