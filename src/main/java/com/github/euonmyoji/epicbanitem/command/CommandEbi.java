package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.command.CommandCallable;
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

/**
 * @author EBI
 */
@NonnullByDefault
public class CommandEbi extends AbstractCommand {

    private Map<List<String>, CommandCallable> childrenMap = new HashMap<>();

    public CommandEbi() {
        super("ebi", "epicbanitem", "banitem", "bi");
        addChildCommand(new CommandReload());
        addChildCommand(new CommandList());
        addChildCommand(new CommandQuery());
        addChildCommand(new CommandShow());
        addChildCommand(new CommandCheck());
        addChildCommand(new CommandCreate());
        //todo:update
        childrenMap.put(Arrays.asList("update", "u"), Update.update);
        //todo:apply
        childrenMap.put(Arrays.asList("apply", "a"), Apply.apply);
        addChildCommand(new CommandHelp(childrenMap));
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

    private static final String ARGUMENT_KEY = "string";

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of(ARGUMENT_KEY)));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        src.sendMessage(getMessage("version", "version", EpicBanItem.VERSION));
        src.sendMessage(getMessage("useHelp", "help_command", "/" + EpicBanItem.plugin.getMainCommandAlias() + " help"));
        args.<String>getOne(ARGUMENT_KEY).ifPresent(s -> {
            String lastMatchCommand = null;
            int lastM = -1;
            for (Map.Entry<List<String>, CommandCallable> entry : childrenMap.entrySet()) {
                if (entry.getValue().testPermission(src)) {
                    try {
                        String command = entry.getKey().get(0);
                        int d = getHowClose(command, s);
                        if (d > lastM) {
                            lastMatchCommand = command;
                            lastM = d;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        EpicBanItem.logger.debug("未知IndexOut情况", e);
                    }
                }
            }
            if (lastMatchCommand != null) {
                src.sendMessage(Text.of(String.format("Do you mean /ebi %s?", lastMatchCommand)));
            }
        });
        return CommandResult.success();
    }

    private void addChildCommand(ICommand command) {
        childrenMap.put(command.getNameList(), command.getCallable());
    }


    private static int getHowClose(String raw, String s) {
        //s长度不大于raw的1.5倍并且不匹配的字符不超过raw字符数 并且至少有一半字符匹配raw 才算接近
        final float offset = 1.5f;
        final int rawLen = raw.length();
        int d = 0;
        if (rawLen * offset >= s.length()) {
            for (int i = 0; i < s.length(); i++) {

                if (s.contains(raw)) {
                    return rawLen + d;
                }

                String c = s.substring(i, i + 1);
                if (raw.contains(c)) {
                    d++;
                    raw = raw.replaceFirst(c, "");
                } else {
                    d--;
                }
            }
        } else {
            return Integer.MIN_VALUE;
        }
        return d;
    }
}
