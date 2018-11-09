package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.*;

@NonnullByDefault
public class CommandEdit extends AbstractCommand {
    private Map<List<String>, CommandCallable> childrenMap = new HashMap<>();

    public CommandEdit() {
        super("edit");
//        addChildCommand(new Query());
        addChildCommand(new Update());
        addChildCommand(new World());
        addChildCommand(new Trigger());
        addChildCommand(new CommandHelp(childrenMap));
        commandSpec = CommandSpec.builder()
                .permission(getPermission("base"))
                .description(getDescription())
                .children(childrenMap)
                .extendedDescription(getExtendedDescription())
                .executor(this)
                .build();
    }

    private void addChildCommand(ICommand command) {
        childrenMap.put(command.getNameList(), command.getCallable());
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // TODO: 2018/10/16 "Text Editor GUI"
        return CommandResult.empty();
    }

    private class Query extends AbstractCommand {

        private Query() {
            super("query");
            parent = CommandEdit.this.name + ".";
        }

        @Override
        public CommandElement getArgument() {
            return seq(
                    EpicBanItemArgs.checkRule(Text.of("rule")),
                    optional(remainingRawJoinedStrings(Text.of("query-rule")))
            );
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            // noinspection ConstantConditions
            CheckRule rule = args.<CheckRule>getOne("rule").get();
            Optional<String> oldId = Optional.ofNullable(rule.getQueryNode().getNode("id").getString());
            String query = args.<String>getOne("query-rule").orElse("{}");
            try {
                ConfigurationNode queryNode = TextUtil.serializeStringToConfigNode(query);
                Optional<String> id = Optional.ofNullable(queryNode.getNode("id").getString());
                // TODO: 2018/10/16 How to deal with id?
            } catch (IOException e) {
                e.printStackTrace();
            }

            return CommandResult.empty();
        }
    }

    private class Update extends AbstractCommand {

        private Update() {
            super("update");
            parent = CommandEdit.this.name + ".";
        }

        @Override
        public CommandElement getArgument() {
            return seq(
                    EpicBanItemArgs.checkRule(Text.of("rule")),
                    optional(remainingRawJoinedStrings(Text.of("query-rule")))
            );
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            // noinspection ConstantConditions
            CheckRule rule = args.<CheckRule>getOne("rule").get();
            String update = args.<String>getOne("update-rule").orElse(null);
            try {
                ConfigurationNode updateNode = null;
                if (update != null) {
                    updateNode = TextUtil.serializeStringToConfigNode(update);
                }
                rule.setUpdateNode(updateNode);
            } catch (IOException e) {
                EpicBanItem.getLogger().error("Error", e);
                throw new CommandException(getMessage("error"), e);
            }
            EpicBanItem.getBanConfig().forceSave();
            src.sendMessage(getMessage("succeed"));
            return CommandResult.success();
        }
    }

    private class World extends AbstractCommand {

        private World() {
            super("world");
            parent = CommandEdit.this.name + ".";
        }

        @Override
        public CommandElement getArgument() {
            return seq(
                    EpicBanItemArgs.checkRule(Text.of("rule")),
                    world(Text.of("world")),
                    EpicBanItemArgs.tristate(Text.of("value"))
            );
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            CheckRule rule = args.<CheckRule>getOne("rule").get();
            WorldProperties world = args.<WorldProperties>getOne("world").get();
            Tristate tristate = args.<Tristate>getOne("value").get();
            if (tristate == Tristate.UNDEFINED) {
                rule.getEnableWorlds().remove(world.getWorldName());
            } else {
                rule.getEnableWorlds().put(world.getWorldName(), tristate.asBoolean());
            }
            EpicBanItem.getBanConfig().forceSave();
            src.sendMessage(getMessage("succeed"));
            return CommandResult.success();
        }
    }

    private class Trigger extends AbstractCommand {

        private Trigger() {
            super("trigger");
            parent = CommandEdit.this.name + ".";
        }

        @Override
        public CommandElement getArgument() {
            return seq(
                    EpicBanItemArgs.checkRule(Text.of("rule")),
                    EpicBanItemArgs.trigger(Text.of("trigger")),
                    EpicBanItemArgs.tristate(Text.of("value"))
            );
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            CheckRule rule = args.<CheckRule>getOne("rule").get();
            String trigger = args.<String>getOne("trigger").get();
            Tristate tristate = args.<Tristate>getOne("value").get();
            if (tristate == Tristate.UNDEFINED) {
                rule.getEnableTriggers().remove(trigger);
            } else {
                rule.getEnableTriggers().put(trigger, tristate.asBoolean());
            }
            EpicBanItem.getBanConfig().forceSave();
            src.sendMessage(getMessage("succeed"));
            return CommandResult.success();
        }
    }
}
