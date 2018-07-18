package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NonnullByDefault
public class CommandEbi extends AbstractCommand {

    private Map<List<String>,CommandCallable> childrenMap = new HashMap<>();

    public CommandEbi() {
        super("ebi","epicbanitem","banitem","bi");
        addChildCommand(new CommandReload());
        addChildCommand(new CommandList());
        addChildCommand(new CommandQuery());
        addChildCommand(new CommandShow());
        addChildCommand(new CommandCheck());
        addChildCommand(new CommandCreate());
        //todo:update
        childrenMap.put(Arrays.asList("update", "u"),Update.update);
        //todo:apply
        childrenMap.put(Arrays.asList("apply", "a"),Apply.apply);
        //todo:help
        commandSpec = CommandSpec.builder()
                .permission(getPermission("base"))
                .description(getDescription())
                .extendedDescription(getExtendedDescription())
                .children(childrenMap)
                .arguments(getArgument())
                .childArgumentParseExceptionFallback(true)
                .executor(this)
                .build();
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("string")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(getMessage("version","version",EpicBanItem.VERSION));
        src.sendMessage(getMessage("useHelp","help_command","/"+EpicBanItem.plugin.getMainCommandAlias()+" help"));
        return CommandResult.success();
    }

    private void addChildCommand(ICommand command){
        childrenMap.put(command.getNameList(), command.getCallable());
    }
}
