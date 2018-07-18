package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Map;

@NonnullByDefault
public class CommandHelp extends AbstractCommand {
    private static final EpicBanItem plugin = EpicBanItem.plugin;
    private Map<List<String>,CommandCallable> childrenMap;

    public CommandHelp(Map<List<String>, CommandCallable> childrenMap) {
        super("help");
        this.childrenMap = childrenMap;
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.none();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Text.Builder builder = Text.builder();
        boolean first = true;
        for(Map.Entry<List<String>,CommandCallable> entry:childrenMap.entrySet()){
            if(entry.getValue().testPermission(src)){
                if(!first){
                    builder.append(Text.NEW_LINE);
                }else {
                    first = false;
                }
                builder.append(
                        Text.of(TextColors.GRAY,"/"+plugin.getMainCommandAlias()+ " "+entry.getKey().get(0)+" "),
                        entry.getValue().getUsage(src),Text.NEW_LINE,
                        entry.getValue().getShortDescription(src).orElse(Text.of("no description"))
                );
            }
        }
        Text text = builder.build();
        if(text.isEmpty()){
            //todo:没有权限执行任何命令
        }else {
            src.sendMessage(text);
        }
        return CommandResult.success();
    }
}
