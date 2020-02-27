package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.locale.LocaleService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandHelp extends AbstractCommand {
    @Inject
    private CommandMapService service;

    @Inject
    private LocaleService localeService;

    CommandHelp() {
        super("help");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optional(
            GenericArguments.choices(Text.of("sub-command"), () -> service.getFlatMap().keySet(), key -> service.getFlatMap().get(key), false)
        );
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        if (args.hasAny("sub-command")) {
            CommandCallable subCommand = args.<CommandCallable>getOne("sub-command").orElseThrow(NoSuchFieldError::new);
            if (subCommand instanceof CommandSpec && ((CommandSpec) subCommand).getExecutor() instanceof AbstractCommand.Help) {
                src.sendMessage(((AbstractCommand.Help) ((CommandSpec) subCommand).getExecutor()).getHelpMessage(src, args));
            } else {
                src.sendMessage(subCommand.getHelp(src).orElse(getMessage("noHelp")));
            }
        } else {
            Text.Builder builder = Text.builder();
            AtomicBoolean first = new AtomicBoolean(true);
            service
                .getChildrenMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().testPermission(src))
                .forEach(
                    entry -> {
                        if (first.get()) {
                            first.set(false);
                        } else {
                            builder.append(Text.NEW_LINE);
                        }
                        builder.append(
                            Text.of(TextColors.GRAY, "/" + CommandEbi.COMMAND_PREFIX + " " + entry.getKey().get(0) + " "),
                            entry.getValue().getUsage(src),
                            Text.NEW_LINE,
                            entry.getValue().getShortDescription(src).orElse(Text.of("no description"))
                        );
                    }
                );

            Text text = builder.build();
            if (text.isEmpty()) {
                src.sendMessage(localeService.getTextWithFallback("epicbanitem.command.help.empty"));
            } else {
                src.sendMessage(text);
            }
        }
        return CommandResult.success();
    }
}
