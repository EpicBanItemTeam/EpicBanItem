package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Map;

/**
 * @author The EpicBanItem Team
 */
@Singleton
public class CommandEbi extends AbstractCommand {
    public static String COMMAND_PREFIX;

    private static final String ARGUMENT_KEY = "string";

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    private CommandMapService service;

    @Inject
    private Logger logger;

    @Inject
    private CommandHelp commandHelp;

    @Inject
    private CommandList commandList;

    @Inject
    private CommandCallback commandCallback;

    @Inject
    private CommandQuery commandQuery;

    @Inject
    private CommandShow commandShow;

    @Inject
    private CommandCheck commandCheck;

    @Inject
    private CommandCheckAll commandCheckAll;

    @Inject
    private CommandCreate commandCreate;

    @Inject
    private CommandUpdate commandUpdate;

    @Inject
    private CommandRemove commandRemove;

    @Inject
    private CommandEdit commandEdit;

    @Inject
    private CommandEditor commandEditor;

    @Inject
    public CommandEbi(EventManager eventManager, PluginContainer pluginContainer) {
        super("ebi", "epicbanitem", "banitem", "bi");
        eventManager.registerListeners(pluginContainer, this);
    }

    private static int getHowClose(String raw, String s) {
        // s长度不大于raw的1.5倍并且不匹配的字符不超过raw字符数 并且至少有一半字符匹配raw 才算接近
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

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of(ARGUMENT_KEY)));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        src.sendMessage(getMessage("version", Tuple.of("version", pluginContainer.getVersion().orElse("@version@"))));
        src.sendMessage(getMessage("useHelp", Tuple.of("help_command", suggestCommand("help"))));
        args
            .<String>getOne(ARGUMENT_KEY)
            .ifPresent(
                s -> {
                    String lastMatchCommand = null;
                    int lastM = -1;
                    int finalLastM = lastM;
                    for (Map.Entry<List<String>, CommandCallable> entry : service.getChildrenMap().entrySet()) {
                        if (entry.getValue().testPermission(src)) {
                            try {
                                String command = entry.getKey().get(0);
                                int d = getHowClose(command, s);
                                if (d > lastM) {
                                    lastMatchCommand = command;
                                    lastM = d;
                                }
                            } catch (IndexOutOfBoundsException e) {
                                logger.debug("Unexpected IndexOutOfBoundsException", e);
                            }
                        }
                    }
                    if (lastMatchCommand != null) {
                        src.sendMessage(getMessage("suggestCommand", Tuple.of("suggest", suggestCommand(lastMatchCommand))));
                    }
                }
            );
        return CommandResult.success();
    }

    private String suggestCommand(String childCommand) {
        return "/" + CommandEbi.COMMAND_PREFIX + " " + childCommand;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        service.registerCommand(commandHelp);
        service.registerCommand(commandList);
        service.registerCommand(commandCallback);
        service.registerCommand(commandQuery);
        service.registerCommand(commandShow);
        service.registerCommand(commandCheck);
        service.registerCommand(commandCheckAll);
        service.registerCommand(commandCreate);
        service.registerCommand(commandUpdate);
        service.registerCommand(commandRemove);
        service.registerCommand(commandEdit);
        service.registerCommand(commandEditor);

        commandSpec =
            CommandSpec
                .builder()
                .permission(getPermission("base"))
                .description(getDescription())
                .extendedDescription(getExtendedDescription())
                .children(service.getChildrenMap())
                .arguments(getArgument())
                .childArgumentParseExceptionFallback(true)
                .executor(this)
                .build();

        Sponge
            .getCommandManager()
            .register(pluginContainer, this.getCallable(), this.getNameList())
            .ifPresent(mapping -> COMMAND_PREFIX = mapping.getPrimaryAlias());
    }
}
