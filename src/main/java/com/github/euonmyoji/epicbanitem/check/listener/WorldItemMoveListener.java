package com.github.euonmyoji.epicbanitem.check.listener;

import com.github.euonmyoji.epicbanitem.api.CheckResult;
import com.github.euonmyoji.epicbanitem.api.CheckRuleService;
import com.github.euonmyoji.epicbanitem.check.Triggers;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.collect.Streams;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent.Teleport;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.World;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@SuppressWarnings("UnstableApiUsage")
public class WorldItemMoveListener {
    private CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Listener
    public void onJoin(Join event, @First Player player) {
        Streams
            .stream(service.checkInventory(player.getInventory(), player.getWorld(), Triggers.JOIN, player))
            .filter(tuple -> tuple.getFirst().isBanned())
            .forEach(
                tuple ->
                    tuple
                        .getFirst()
                        .getFinalView()
                        .map(dataContainer -> NbtTagDataUtil.toItemStack(dataContainer, tuple.getSecond().peek().get().getQuantity()))
                        .ifPresent(
                            finalItem -> {
                                CheckResult checkResult = tuple.getFirst();
                                Inventory inventory = tuple.getSecond();
                                ItemStack itemStack = inventory.peek().get();
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
                        )
            );
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Listener(order = Order.FIRST)
    public void handleChangeWorld(Teleport event, @Getter("getToTransform") Transform<World> toTransform, @Getter("getTargetEntity") Entity entity) {
        if (!(entity instanceof Carrier)) return;
        Carrier carrier = (Carrier) entity;
        World world = toTransform.getExtent();
        this.getCorrectEntity(entity, Subject.class)
            .ifPresent(
                subject ->
                    Streams
                        .stream(service.checkInventory(carrier.getInventory(), world, Triggers.JOIN, subject))
                        .filter(tuple -> tuple.getFirst().isBanned())
                        .forEach(
                            tuple ->
                                tuple
                                    .getFirst()
                                    .getFinalView()
                                    .map(dataContainer -> NbtTagDataUtil.toItemStack(dataContainer, tuple.getSecond().peek().get().getQuantity()))
                                    .ifPresent(
                                        finalItem -> {
                                            CheckResult checkResult = tuple.getFirst();
                                            Inventory inventory = tuple.getSecond();
                                            ItemStack itemStack = inventory.peek().get();
                                            inventory.set(finalItem);
                                            getCorrectEntity(entity, MessageReceiver.class)
                                                .ifPresent(
                                                    receiver ->
                                                        TextUtil
                                                            .prepareMessage(
                                                                Triggers.JOIN,
                                                                TextUtil.getDisplayName(itemStack),
                                                                TextUtil.getDisplayName(finalItem),
                                                                ((CheckResult.Banned) checkResult).getBanRules(),
                                                                checkResult.isUpdateNeeded()
                                                            )
                                                            .forEach(receiver::sendMessage)
                                                );
                                            event.setCancelled(true);
                                        }
                                    )
                        )
            );
    }

    private <T> Optional<T> getCorrectEntity(Entity entity, Class<T> clazz) {
        Optional<T> entityOptional;

        if (clazz.isInstance(entity)) entityOptional = Optional.of(clazz.cast(entity)); else entityOptional =
            entity.getCreator().flatMap(Sponge.getServer()::getPlayer).map(clazz::cast);

        return entityOptional;
    }
}
