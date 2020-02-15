package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleIndex;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
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
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@Singleton
@NonnullByDefault
class CommandList extends AbstractCommand {
    @Inject
    private CheckRuleService service;

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
                lines.add(
                    Text.of(
                        TextUtil.adjustLength(getMessage("firstHalfLine", "item_type", entry.getKey(), "check_rule", checkRule.toText()), 20),
                        getMessage("secondHalfLine", "item_type", entry.getKey(), "check_rule", checkRule.toText())
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
