package team.ebi.epicbanitem.check.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.AffectItemStackEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent.Transfer;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.TargetInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.WornEquipmentType;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.CheckResult;
import team.ebi.epicbanitem.api.CheckRuleTrigger;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.check.Triggers;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.TextUtil;

/**
 * @author The EpicBanItem Team
 */
@SuppressWarnings({ "DuplicatedCode", "UnstableApiUsage" })
@Singleton
// TODO: 2020/2/21 Logger I18N
public class InventoryListener {
    @Inject
    private Logger logger;

    @Inject
    private CheckRuleService service;

    @Inject
    public InventoryListener(PluginContainer pluginContainer, EventManager eventManager) {
        eventManager.registerListeners(pluginContainer, this);
    }

    private static ItemStack getItem(DataContainer view, int quantity) {
        return NbtTagDataUtil.toItemStack(view, quantity);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onCrafting(
        AffectItemStackEvent event,
        @Getter("getCause") Cause cause,
        @Getter("getTransactions") List<? extends Transaction<ItemStackSnapshot>> transactions
    ) {
        if (EpicBanItem.getSettings().isCraftingEventClass(event)) {
            List<World> worlds = Lists.newArrayList();
            Optional<Player> playerOptional = cause.first(Player.class);
            playerOptional.map(Locatable::getWorld).ifPresent(worlds::add);

            if (worlds.isEmpty()) {
                if (event instanceof TargetInventoryEvent) {
                    Inventory inventory = ((TargetInventoryEvent) event).getTargetInventory();
                    if (inventory instanceof Container) {
                        ((Container) inventory).getViewers().stream().map(Locatable::getWorld).forEach(worlds::add);
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
                                logger.warn("EpicBanItem cannot even find a world! What is the server doing?");
                                logger.debug("No world found in " + cause.toString(), e);
                                return def;
                            }
                        )
                );
            }

            transactions.forEach(
                (Transaction<ItemStackSnapshot> transaction) -> {
                    ItemStackSnapshot finalItem = transaction.getFinal();
                    worlds
                        .stream()
                        .map(world -> service.check(finalItem, world, Triggers.CRAFT, playerOptional.orElse(null)))
                        .filter(CheckResult::isBanned)
                        .map(CheckResult::getFinalView)
                        .map(optional -> optional.map(dataContainer -> getItem(dataContainer, finalItem.getQuantity()).createSnapshot()))
                        .map(optional -> optional.orElse(ItemStackSnapshot.NONE))
                        .findFirst()
                        .ifPresent(transaction::setCustom);
                }
            );
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
    @Exclude({ ClickInventoryEvent.Drop.class, ClickInventoryEvent.Creative.class, ClickInventoryEvent.Recipe.class })
    public void onTransferClick(
        ClickInventoryEvent event,
        @Getter("getTargetInventory") Container container,
        @First Player player,
        @Getter("getTransactions") List<SlotTransaction> transactions
    ) {
        if (Streams.stream(container.iterator()).count() < 2) {
            return;
        }

        Optional<Inventory> targetInventory = transactions
            .stream()
            .filter(slotTransaction -> !slotTransaction.getFinal().isEmpty())
            .filter(slotTransaction -> slotTransaction.getOriginal().getQuantity() < slotTransaction.getFinal().getQuantity())
            .map(SlotTransaction::getSlot)
            .map(Slot::transform)
            .map(Inventory::parent)
            .findFirst();
        Inventory sourceInventory = transactions
            .stream()
            .filter(slotTransaction -> !slotTransaction.getOriginal().isEmpty())
            .filter(slotTransaction -> slotTransaction.getOriginal().getQuantity() > slotTransaction.getFinal().getQuantity())
            .map(SlotTransaction::getSlot)
            .map(Slot::transform)
            .map(Inventory::parent)
            .findFirst()
            .orElse(
                Streams
                    // get the second(down) inventory
                    .stream(Streams.stream(container.iterator()).skip(1).findFirst().get().slots().iterator())
                    .map(Slot.class::cast)
                    .map(Slot::transform)
                    .map(Inventory::parent)
                    .findFirst()
                    .get()
            );
        if (targetInventory.isPresent() && !Objects.equals(targetInventory.get(), sourceInventory)) {
            if (this.checkInventory(player, Triggers.STORE, transactions.stream())) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onTransfer(
        Transfer event,
        @Getter("getTransactions") List<SlotTransaction> transactions,
        @Getter("getSource") Locatable locatable,
        @Getter("getContext") EventContext context
    ) {
        CheckRuleTrigger trigger = Triggers.STORE;
        Optional<User> userOptional = context.get(EventContextKeys.OWNER);
        for (SlotTransaction tran : transactions) {
            ItemStackSnapshot item = tran.getOriginal();
            CheckResult result = service.check(item, locatable.getWorld(), trigger, userOptional.orElse(null));
            if (result.isBanned()) {
                Optional<ItemStack> optionalFinalItem = result.getFinalView().map(view -> getItem(view, item.getQuantity()));
                if (optionalFinalItem.isPresent()) {
                    tran.setCustom(optionalFinalItem.get().createSnapshot());
                } else {
                    event.setCancelled(true);
                }

                userOptional
                    .flatMap(User::getPlayer)
                    .ifPresent(
                        player -> {
                            Text originItemName = TextUtil.getDisplayName(item.createStack());
                            Text finalItemName = TextUtil.getDisplayName(optionalFinalItem.orElse(item.createStack()));
                            TextUtil
                                .prepareMessage(
                                    trigger,
                                    originItemName,
                                    finalItemName,
                                    ((CheckResult.Banned) result).getBanRules(),
                                    result.isUpdateNeeded()
                                )
                                .forEach(player::sendMessage);
                        }
                    );
            }
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
            Text originItemName = TextUtil.getDisplayName(itemEntity);
            Text finalItemName = TextUtil.getDisplayName(optionalFinalItem.orElse(item.createStack()));
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

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(InteractBlockEvent event, @First Player player) {
        event
            .getContext()
            .get(EventContextKeys.USED_ITEM)
            .filter(item -> checkUseItem(player, Triggers.USE, ((HandInteractEvent) event).getHandType(), item))
            .ifPresent(item -> event.setCancelled(true));

        if (!event.isCancelled()) {
            CheckRuleTrigger trigger = Triggers.INTERACT;
            checkInteractBlock(player, trigger, event.getTargetBlock());
        }
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
            Text originName = TextUtil.getDisplayName(item.createStack());
            Text finalItemName = TextUtil.getDisplayName(finalItem.orElse(item.createStack()));
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
                                TextUtil.getDisplayName(item.createStack()),
                                TextUtil.getDisplayName(finalItem),
                                ((CheckResult.Banned) result).getBanRules(),
                                result.isUpdateNeeded()
                            )
                            .forEach(player::sendMessage);
                        tran.setCustom(finalItem.createSnapshot());
                    } else {
                        Text itemName = TextUtil.getDisplayName(item.createStack());
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
