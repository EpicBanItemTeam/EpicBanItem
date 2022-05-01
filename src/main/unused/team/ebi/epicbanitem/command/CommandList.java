package team.ebi.epicbanitem.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import team.ebi.epicbanitem.check.CheckRule;
import team.ebi.epicbanitem.check.CheckRuleService;
import team.ebi.epicbanitem.command.arg.EpicBanItemArgs;
import team.ebi.epicbanitem.util.TextUtil;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
class CommandList extends AbstractCommand {
    @Inject
    private CheckRuleService service;

    @Inject
    private CommandEdit commandEdit;

    CommandList() {
        super("list", "l");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optional(EpicBanItemArgs.itemOrHand(Text.of("item-type"), true));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        LinkedHashMap<String, List<CheckRule>> toShow = new LinkedHashMap<>();
        Stream<CheckRuleIndex> indexStream;
        if (args.hasAny("item-type")) {
            ItemType itemType = args.<ItemType>getOne("item-type").orElseThrow(NoSuchFieldError::new);
            indexStream = Stream.of(CheckRuleIndex.of(), CheckRuleIndex.of(itemType));
        } else {
            indexStream = Stream.concat(Stream.of(CheckRuleIndex.of()), service.getIndexes().stream());
        }
        List<Text> lines = new ArrayList<>();
        indexStream.forEach(i -> toShow.put(i.toString(), service.getCheckRulesByIndex(i)));
        for (Map.Entry<String, List<CheckRule>> entry : toShow.entrySet()) {
            for (CheckRule checkRule : entry.getValue()) {
                Text ruleName = getMessage("firstHalfLine", Tuple.of("item_type", entry.getKey()), Tuple.of("check_rule", checkRule.toText()))
                    .toBuilder()
                    .onHover(TextActions.showText(getMessage("clickToEdit")))
                    .onClick(TextActions.runCommand(commandEdit.getCommandString() + checkRule.getName().toString()))
                    .build();
                lines.add(
                    Text.of(
                        TextUtil.adjustLength(ruleName, 20),
                        getMessage("secondHalfLine", Tuple.of("item_type", entry.getKey()), Tuple.of("check_rule", checkRule.toText()))
                    )
                );
            }
        }

        if (lines.isEmpty()) {
            lines.add(getMessage("noRule"));
        }

        PaginationList.Builder paginationList = PaginationList.builder().title(getMessage("title")).contents(lines).padding(getMessage("padding"));
        if (src instanceof Player) {
            paginationList.linesPerPage(15);
        } else {
            paginationList.linesPerPage(lines.size() + 5);
        }
        // TODO: 点击补全的命令
        paginationList.build().sendTo(src);
        return CommandResult.success();
    }
}
