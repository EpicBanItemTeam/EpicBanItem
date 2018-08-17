package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author EBI
 */
@NonnullByDefault
public class CommandReload extends AbstractCommand {
    public CommandReload() {
        super("load", "r");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return CommandResult.success(); /* TODO: Remove this command
        try {
            EpicBanItem.plugin.reload();
            src.sendMessage(getMessage("succeed"));
            return CommandResult.success();
        } catch (IOException e) {
            EpicBanItem.logger.warn(getMessage("failed").toPlain(), e);
            throw new CommandException(getMessage("failed"), e);
        }
        */
    }
}
