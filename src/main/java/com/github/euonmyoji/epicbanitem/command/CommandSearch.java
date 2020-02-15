package com.github.euonmyoji.epicbanitem.command;

import static org.spongepowered.api.command.args.GenericArguments.seq;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

//todo: ebi2.0?
/**
 * ebi search corner1 corner2 numPerTick checkRule
 * ebi search corner1 corner2 numPerTick [checkRule|checkRule::query|queryByString] (checkRule::update|updateByString)
 * @author yinyangshi
 */
public class CommandSearch extends AbstractCommand {

    public CommandSearch() {
        super("search");
    }

    @Override
    public CommandElement getArgument() {
        return seq(
            GenericArguments.location(Text.of("corner1")),
            GenericArguments.location(Text.of("corner2")),
            GenericArguments.integer(Text.of("numPerTick"))
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return null;
    }
}
