package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.remainingRawJoinedStrings;

/**
 * @author EBI
 */
class CommandCreate extends AbstractCommand {

    public CommandCreate() {
        super("create", "c");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.seq(
                EpicBanItemArgs.itemOrHand(Text.of("item-type"), false),
                GenericArguments.string(Text.of("rule-name")),
                optional(remainingRawJoinedStrings(Text.of("query-rule")))
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
//        throw new CommandException(Text.of("Not Support Yet.")); ???? 这什么玩意啊 咸鱼姐姐
        ItemType itemType = args.<ItemType>getOne("item-type").get();
        String ruleName = args.<String>getOne("rule-name").get();
        // TODO: use histories in Query?
        String rule = args.<String>getOne("query-rule").orElse("");
        try {
            CheckRule checkRule;
            if (!rule.isEmpty()) {
                ConfigurationNode node = TextUtil.serializeStringToConfigNode(rule);
                checkRule = new CheckRule(ruleName, node);
            } else {
                checkRule = new CheckRule(ruleName);
            }
            Sponge.getServiceManager().provideUnchecked(CheckRuleService.class).addRule(itemType, checkRule);
        } catch (Exception e) {
            throw new CommandException(getMessage("failed"), e);
        }
        src.sendMessage(getMessage("succeed", "rule_name", ruleName));
        return CommandResult.success();
    }
}
