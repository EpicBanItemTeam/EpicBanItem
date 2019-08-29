package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.collect.Iterables;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Predicate;

import static org.spongepowered.api.command.args.GenericArguments.*;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
class CommandCreate extends AbstractCommand {

    CommandCreate() {
        super("create", "c");
    }

    static Optional<BlockSnapshot> getBlockLookAt(CommandSource src) {
        if (src instanceof Entity) {
            Predicate<BlockRayHit<World>> filter = BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1);
            BlockRay.BlockRayBuilder<World> blockRayBuilder = BlockRay.from((Entity) src).stopFilter(filter);
            return blockRayBuilder.distanceLimit(5).build().end().map(h -> h.getLocation().createSnapshot());
        }
        return Optional.empty();
    }

    static Optional<Tuple<HandType, ItemStack>> getItemInHand(CommandSource src) {
        if (src instanceof ArmorEquipable && src instanceof Locatable) {
            for (HandType handType : Sponge.getRegistry().getAllOf(HandType.class)) {
                Optional<ItemStack> handItem = ((ArmorEquipable) src).getItemInHand(handType);
                if (handItem.isPresent() && !handItem.get().isEmpty()) {
                    return Optional.of(Tuple.of(handType, handItem.get()));
                }
            }
        }
        return Optional.empty();
    }

    static void setItemInHand(CommandSource src, Tuple<HandType, ItemStack> item) {
        if (src instanceof ArmorEquipable && src instanceof Locatable) {
            ((ArmorEquipable) src).setItemInHand(item.getFirst(), item.getSecond());
        }
    }

    @Override
    public CommandElement getArgument() {
        return seq(string(Text.of("rule-name")), flags()
                .flag("-no-capture").flag("-simple-capture").flag("-all-capture").flag("-all-match")
                .buildWith(optional(remainingRawJoinedStrings(Text.of("query-rule")))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Set<Predicate<Map.Entry<DataQuery, Object>>> captureMethods = new HashSet<>();
        if (args.hasAny("no-capture")) {
            captureMethods.add(e -> false);
        }
        if (args.hasAny("simple-capture")) {
            captureMethods.add(e -> e.getValue() instanceof Number || e.getValue() instanceof String);
        }
        if (args.hasAny("all-capture") || args.hasAny("all-match")) {
            captureMethods.add(e -> Objects.nonNull(e.getValue()));
        }
        String name = args.<String>getOne("rule-name").orElseThrow(() -> new IllegalArgumentException("What's the sponge version?, EpicBanItem cannot find a rule-name!"));
        String query = args.<String>getOne("query-rule").orElse("{}");
        Predicate<Map.Entry<DataQuery, Object>> capture = e -> "id".equals(e.getKey().toString());
        try {
            CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
            if (service.getCheckRuleByName(name).isPresent()) {
                throw new CommandException(getMessage("existed", "rule_name", name));
            }
            Optional<Tuple<HandType, ItemStack>> handItem = getItemInHand(src);
            try {
                capture = Iterables.getOnlyElement(captureMethods, capture);
            } catch (IllegalArgumentException e) {
                throw new CommandException(getMessage("conflict"));
            }
            ConfigurationNode queryNode = TextUtil.serializeStringToConfigNode(query);
            DataView nbt = handItem.map(e -> NbtTagDataUtil.toNbt(e.getSecond())).orElse(DataContainer.createNew());
            for (Map.Entry<DataQuery, Object> entry : nbt.getValues(false).entrySet()) {
                //noinspection ConstantConditions interesting idea
                if (!capture.test(entry)) {
                    nbt.remove(entry.getKey());
                }
            }
            ConfigurationNode nbtNode = DataTranslators.CONFIGURATION_NODE.translate(nbt);
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtNode.getChildrenMap().entrySet()) {
                Object key = entry.getKey();
                ConfigurationNode node = queryNode.getNode(key);
                if (node.isVirtual()) {
                    node.setValue(entry.getValue());
                } else {
                    throw new CommandException(getMessage("override", "key", key.toString()));
                }
            }
            if (queryNode.getNode("id").isVirtual() && !args.hasAny("no-capture")) {
                throw new CommandException(getMessage("empty"));
            }
            if (src instanceof Player) {
                CommandEditor.add((Player) src, name, queryNode, true);
            } else {
                service.appendRule(new CheckRule(name, queryNode)).thenRun(() -> {
                    Text succeedMessage = getMessage("succeed", "rule_name", name);
                    src.sendMessage(succeedMessage);
                });
            }
            return CommandResult.success();
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            EpicBanItem.getLogger().error("Failed to create.", e);
            throw new CommandException(getMessage("failed"), e);
        }
    }
}
