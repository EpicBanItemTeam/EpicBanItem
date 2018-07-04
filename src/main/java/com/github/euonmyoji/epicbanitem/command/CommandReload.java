package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;

@NonnullByDefault
public class CommandReload extends AbstractCommand {
    public CommandReload() {
        super("reload", "r");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        try {
            EpicBanItem.plugin.reload();
            src.sendMessage(getMessage("succeed"));
            return CommandResult.success();
        } catch (IOException|ObjectMappingException e) {
            throw new CommandException(getMessage("failed"), e);
        }
    }
}
