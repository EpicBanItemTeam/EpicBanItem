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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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
        SortedMap<String, CommandCallable> flatMap = service.getFlatMap();
        return GenericArguments.optional(
            GenericArguments.choices(Text.of("sub-command"), flatMap::keySet, flatMap::get, false)
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
            Text.Builder textBuilder = Text.builder()
                    .append(Text.of("EpicBanItem Help"))
                    .append(Text.NEW_LINE)
                    .append(localeService.getText("epicbanitem.command.editor.header").orElse(Text.NEW_LINE));
            for (Map.Entry<List<String>, CommandCallable> entry : service.getChildrenMap().entrySet()) {
                textBuilder
                        .append(Text.NEW_LINE)
                        .append(Text
                                .builder("/" + CommandEbi.COMMAND_PREFIX + " " + entry.getKey().get(0) + " ")
                                .color(TextColors.LIGHT_PURPLE)
                                .build())
                        .append(entry.getValue().getUsage(src)
                                .toBuilder()
                                .color(TextColors.AQUA)
                                .build());
                textBuilder
                        .append(Text.NEW_LINE)
                        .append(entry.getValue().getShortDescription(src).orElse(Text.of("no description")));
            }
            Text text = textBuilder.build();
            if (text.isEmpty()) {
                src.sendMessage(localeService.getTextWithFallback("epicbanitem.command.help.empty"));
            } else {
                src.sendMessage(text);
            }
        }
        return CommandResult.success();
    }
}
