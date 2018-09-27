package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.*;

/**
 * @author EBI
 */
@NonnullByDefault
class CommandCreate extends AbstractCommand {

    CommandCreate() {
        super("create", "c");
    }

    @Override
    public CommandElement getArgument() {
        return seq(string(Text.of("rule-name")),
                flags().flag("-no-capture").buildWith(none()),
                optional(remainingRawJoinedStrings(Text.of("query-rule"))));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        boolean noCapture = args.hasAny("no-capture");
        // noinspection ConstantConditions
        String name = args.<String>getOne("rule-name").get();
        // TODO: use histories in Query?
        String query = args.<String>getOne("query-rule").orElse("{}");
        try {
            ConfigurationNode queryNode = TextUtil.serializeStringToConfigNode(query);
            Optional<String> id = Optional.ofNullable(queryNode.getNode("id").getString());
            CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
            Optional<ItemStack> handItem = src instanceof ArmorEquipable ? getItemInHand((ArmorEquipable) src) : Optional.empty();
            if (!noCapture) {
                if (handItem.isPresent()) {
                    if (!id.isPresent()) {
                        String s = handItem.get().getType().getId();
                        queryNode.getNode("id").setValue(s);
                        id = Optional.of(s);
                    } else {
                        throw new CommandException(getMessage("override"));
                    }
                } else {
                    if (!id.isPresent()) {
                        throw new CommandException(getMessage("empty"));
                    }
                }
            }
            CheckRule checkRule = new CheckRule(name, queryNode);
            service.addRule(id.flatMap(s -> Sponge.getRegistry().getType(ItemType.class, s)).orElse(null), checkRule);
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(getMessage("failed"), e);
        }
        src.sendMessage(getMessage("succeed", "rule_name", name));
        return CommandResult.success();
    }

    private static Optional<ItemStack> getItemInHand(ArmorEquipable armorEquipable) {
        for (HandType handType : Sponge.getRegistry().getAllOf(HandType.class)) {
            Optional<ItemStack> handItem = armorEquipable.getItemInHand(handType);
            if (handItem.isPresent() && !handItem.get().isEmpty()) {
                return handItem;
            }
        }
        return Optional.empty();
    }
}
