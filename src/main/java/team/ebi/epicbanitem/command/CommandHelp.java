package team.ebi.epicbanitem.command;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.locale.LocaleService;

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
        return GenericArguments.optional(GenericArguments.choices(Text.of("sub-command"), flatMap::keySet, flatMap::get, false));
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
            List<Text> helpTexts = Lists.newArrayList();
            for (Map.Entry<List<String>, CommandCallable> entry : service.getChildrenMap().entrySet()) {
                Text.Builder textBuilder = Text.builder();
                textBuilder
                    .append(
                        Text
                            .builder("/" + CommandEbi.COMMAND_PREFIX + " " + entry.getKey().get(0) + " ")
                            .onHover(TextActions.showText(getMessage("clickToDetail")))
                            .onClick(TextActions.runCommand(this.getCommandString() + entry.getKey().get(0)))
                            .color(TextColors.LIGHT_PURPLE)
                            .build()
                    )
                    .append(entry.getValue().getUsage(src).toBuilder().color(TextColors.AQUA).build());
                textBuilder.append(Text.NEW_LINE).append(entry.getValue().getShortDescription(src).orElse(Text.of("no description")));
                helpTexts.add(textBuilder.build());
            }
            if (helpTexts.isEmpty()) {
                src.sendMessage(localeService.getTextWithFallback("epicbanitem.command.help.empty"));
            } else {
                Builder builder = PaginationList.builder();
                localeService.getText("epicbanitem.command.ebi.description").ifPresent(builder::title);
                builder.padding(Text.of(TextColors.GREEN, "-")).contents(helpTexts).sendTo(src);
            }
        }
        return CommandResult.success();
    }
}
