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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author EBI
 */
@NonnullByDefault
public class CommandHelp extends AbstractCommand {
    private static final EpicBanItem plugin = EpicBanItem.plugin;
    private Map<List<String>, CommandCallable> childrenMap;
    private Map<String, CommandCallable> flatMap;

    public CommandHelp(Map<List<String>, CommandCallable> childrenMap) {
        super("help");
        this.childrenMap = childrenMap;
        this.flatMap = new LinkedHashMap<>();
        for (Map.Entry<List<String>, CommandCallable> entry : childrenMap.entrySet()) {
            for (String s : entry.getKey()) {
                flatMap.put(s, entry.getValue());
            }
        }
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optional(GenericArguments.choices(Text.of("sub-command"), flatMap, false, false));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (args.hasAny("sub-command")) {
            //noinspection ConstantConditions
            CommandCallable subCommand = args.<CommandCallable>getOne("sub-command").get();
            if (subCommand instanceof CommandSpec && ((CommandSpec) subCommand).getExecutor() instanceof AbstractCommand.Help) {
                src.sendMessage(((AbstractCommand.Help) ((CommandSpec) subCommand).getExecutor()).getHelpMessage(src, args));
            } else {
                src.sendMessage(subCommand.getHelp(src).orElse(getMessage("noHelp")));
            }
        } else {
            Text.Builder builder = Text.builder();
            boolean first = true;
            for (Map.Entry<List<String>, CommandCallable> entry : childrenMap.entrySet()) {
                if (entry.getValue().testPermission(src)) {
                    if (!first) {
                        builder.append(Text.NEW_LINE);
                    } else {
                        first = false;
                    }
                    builder.append(
                            Text.of(TextColors.GRAY, "/" + plugin.getMainCommandAlias() + " " + entry.getKey().get(0) + " "),
                            entry.getValue().getUsage(src), Text.NEW_LINE,
                            entry.getValue().getShortDescription(src).orElse(Text.of("no description"))
                    );
                }
            }
            Text text = builder.build();
            if (text.isEmpty()) {
                // TODO: 没有权限执行任何命令
            } else {
                src.sendMessage(text);
            }
        }
        return CommandResult.success();
    }
}
