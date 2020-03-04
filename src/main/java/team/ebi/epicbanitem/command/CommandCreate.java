package team.ebi.epicbanitem.command;

import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.remainingRawJoinedStrings;
import static org.spongepowered.api.command.args.GenericArguments.seq;
import static org.spongepowered.api.util.blockray.BlockRay.continueAfterFilter;
import static org.spongepowered.api.util.blockray.BlockRay.onlyAirFilter;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
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
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import team.ebi.epicbanitem.api.CheckRuleLocation;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;
import team.ebi.epicbanitem.util.NbtTagDataUtil;
import team.ebi.epicbanitem.util.TextUtil;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
class CommandCreate extends AbstractCommand {
    @Inject
    private CheckRuleService service;

    CommandCreate() {
        super("create", "c");
    }

    static Optional<BlockSnapshot> getBlockLookAt(CommandSource src) {
        if (src instanceof Entity) {
            Predicate<BlockRayHit<World>> filter = continueAfterFilter(BlockRay.onlyAirFilter(), 1);
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
        return seq(
            EpicBanItemArgs.patternString(Text.of("rule-name"), CheckRuleLocation.NAME_PATTERN),
            flags()
                .flag("-block-inv")
                .flag("-no-capture")
                .flag("-simple-capture")
                .flag("-all-capture")
                .flag("-all-match")
                .buildWith(optional(remainingRawJoinedStrings(Text.of("query-rule"))))
        );
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
        String name = args
            .<String>getOne("rule-name")
            .orElseThrow(() -> new IllegalArgumentException("What's the sponge version?, EpicBanItem cannot find a rule-name!"));
        String query = args.<String>getOne("query-rule").orElse("{}");
        Predicate<Map.Entry<DataQuery, Object>> capture = e -> "id".equals(e.getKey().toString());
        try {
            CheckRuleLocation location = CheckRuleLocation.of(name);
            if (service.getCheckRuleByName(location).isPresent()) {
                throw new CommandException(getMessage("existed", Tuple.of("rule_name", name)));
            }

            try {
                capture = Iterables.getOnlyElement(captureMethods, capture);
            } catch (IllegalArgumentException e) {
                throw new CommandException(getMessage("conflict"));
            }

            final Predicate<Map.Entry<DataQuery, Object>> finalCapture = capture.negate();

            ConfigurationNode queryNode = SimpleConfigurationNode.root();

            if (args.hasAny("block-inv")) {
                if (!(src instanceof Player)) {
                    throw new CommandException(getMessage("playerOnly"));
                }

                Player player = (Player) src;
                BlockRay<World> blockRay = BlockRay.from(player).distanceLimit(5).stopFilter(continueAfterFilter(onlyAirFilter(), 1)).build();
                BlockRayHit<World> rayHit = blockRay.end().orElseThrow(() -> new CommandException(getMessage("emptyBlock")));
                List<ConfigurationNode> nodeList = rayHit
                    .getExtent()
                    .getTileEntity(rayHit.getBlockX(), rayHit.getBlockY(), rayHit.getBlockZ())
                    .map(Carrier.class::cast)
                    .map(Carrier::getInventory)
                    .filter(inventory -> inventory.totalItems() > 0)
                    .map(NbtTagDataUtil::toNbt)
                    .map(Collection::stream)
                    .map(stream -> stream.map(dataContainer -> translateNBTToNode(dataContainer, finalCapture)).collect(Collectors.toList()))
                    .orElseThrow(() -> new CommandException(getMessage("emptyBlock")));
                if (nodeList.isEmpty()) {
                    throw new CommandException(getMessage("emptyBlock"));
                }
                queryNode.getNode("$or").setValue(nodeList);
            } else {
                Optional<Tuple<HandType, ItemStack>> handItem = getItemInHand(src);
                queryNode = TextUtil.serializeStringToConfigNode(query);
                DataView nbt = handItem.map(e -> NbtTagDataUtil.toNbt(e.getSecond())).orElse(DataContainer.createNew());
                ConfigurationNode nbtNode = translateNBTToNode(nbt, finalCapture);
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtNode.getChildrenMap().entrySet()) {
                    Object key = entry.getKey();
                    ConfigurationNode node = queryNode.getNode(key);
                    if (node.isVirtual()) {
                        node.setValue(entry.getValue());
                    } else {
                        throw new CommandException(getMessage("override", Tuple.of("key", key.toString())));
                    }
                }
                if (queryNode.getNode("id").isVirtual() && !args.hasAny("no-capture")) {
                    throw new CommandException(getMessage("empty"));
                }
            }
            if (src instanceof Player) {
                CommandEditor.add((Player) src, location, queryNode, true);
            } else {
                service
                    .appendRule(CheckRule.builder(location).queryNode(queryNode).build())
                    .thenRun(
                        () -> {
                            Text succeedMessage = getMessage("succeed", Tuple.of("rule_name", name));
                            src.sendMessage(succeedMessage);
                        }
                    );
            }
            return CommandResult.success();
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(src, getMessage("failed"), e);
        }
    }

    public ConfigurationNode translateNBTToNode(DataView nbt, Predicate<Map.Entry<DataQuery, Object>> capture) {
        nbt.getValues(false).entrySet().stream().filter(capture).map(Entry::getKey).forEach(nbt::remove);
        return DataTranslators.CONFIGURATION_NODE.translate(nbt);
    }
}
