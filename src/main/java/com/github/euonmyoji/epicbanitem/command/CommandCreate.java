package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
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

import java.util.Optional;
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
        return seq(string(Text.of("rule-name")),
                flags().flag("-no-capture")
                        .flag("-all-match")
                        .buildWith(optional(remainingRawJoinedStrings(Text.of("query-rule")))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        boolean noCapture = args.hasAny("no-capture");
        boolean allMatch = args.hasAny("all-match");
        //noinspection OptionalGetWithoutIsPresent
        String name = args.<String>getOne("rule-name").get();
        String query = args.<String>getOne("query-rule").orElse("{}");
        if (noCapture && allMatch) {
            throw new CommandException(getMessage("conflict"));
        }
        try {
            CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
            Optional<Tuple<HandType, ItemStack>> handItem = getItemInHand(src);
            ConfigurationNode queryNode;
            if (allMatch) {
                if (handItem.isPresent()) {
                    queryNode = DataTranslators.CONFIGURATION_NODE.translate(NbtTagDataUtil.toNbt(handItem.get().getSecond()));
                } else {
                    throw new CommandException(getMessage("noItem"));
                }
            } else {
                queryNode = TextUtil.serializeStringToConfigNode(query);
                Optional<String> id = Optional.ofNullable(queryNode.getNode("id").getString());
                if (!noCapture) {
                    if (handItem.isPresent()) {
                        if (id.isPresent()) {
                            throw new CommandException(getMessage("override"));
                        } else {
                            String s = handItem.get().getSecond().getType().getId();
                            queryNode.getNode("id").setValue(s);
                        }
                    } else {
                        if (!id.isPresent()) {
                            throw new CommandException(getMessage("empty"));
                        }
                    }
                }
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
