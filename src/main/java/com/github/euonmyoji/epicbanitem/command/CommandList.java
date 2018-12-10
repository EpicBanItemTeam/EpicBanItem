package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleIndex;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
class CommandList extends AbstractCommand {

    public CommandList() {
        super("list", "l");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optional(EpicBanItemArgs.itemOrHand(Text.of("item-type"), true));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        LinkedHashMap<String, List<CheckRule>> toShow = new LinkedHashMap<>();
        Stream<CheckRuleIndex> indexStream;
        if (args.hasAny("item-type")) {
            // noinspection ConstantConditions
            ItemType itemType = args.<ItemType>getOne("item-type").get();
            indexStream = Stream.of(CheckRuleIndex.of(), CheckRuleIndex.of(itemType));
        } else {
            indexStream = Stream.concat(Stream.of(CheckRuleIndex.of()), service.getIndexes().stream());
        }
        List<Text> lines = new ArrayList<>();
        indexStream.forEach(i -> toShow.put(i.toString(), service.getCheckRulesByIndex(i)));
        for (Map.Entry<String, List<CheckRule>> entry : toShow.entrySet()) {
            for (CheckRule checkRule : entry.getValue()) {
                lines.add(Text.of(
                        TextUtil.adjustLength(getMessage("firstHalfLine", "item_type", entry.getKey(), "check_rule", checkRule.toText()), 20),
                        getMessage("secondHalfLine", "item_type", entry.getKey(), "check_rule", checkRule.toText())
                ));
            }
        }

        if (lines.isEmpty()) {
            lines.add(getMessage("noRule"));
        }

        PaginationList.Builder paginationList = PaginationList.builder()
                .title(getMessage("title"))
                .contents(lines)
                .padding(getMessage("padding"));
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
