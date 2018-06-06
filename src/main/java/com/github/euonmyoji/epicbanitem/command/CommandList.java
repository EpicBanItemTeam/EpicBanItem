package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import com.github.euonmyoji.epicbanitem.command.arg.EpicBanItemArgs;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NonnullByDefault
class CommandList extends AbstractCommand {

    public CommandList() {
        super("list","l");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.optional(EpicBanItemArgs.itemOrHand(Text.of("item-type"),true));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args){
        CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
        LinkedHashMap<ItemType,List<CheckRule>> toShow = new LinkedHashMap<>();
        if(args.hasAny("item-type")){
            //noinspection ConstantConditions
            ItemType itemType = args.<ItemType>getOne("item-type").get();
            List<CheckRule> rules = service.getCheckRules(itemType);
            toShow.put(itemType,rules);
        }else {
            //all
            for(ItemType itemType:service.getCheckItemTypes()){
                toShow.put(itemType,service.getCheckRules(itemType));
            }
        }
        // ============= minecraft:dummy_item =============
        // rule_name:
        //   worlds:[]
        //   triggers:use,throw,pickup,transfer,click   //颜色区分是否开启?
        //   remove: false
        //   query:{}
        //   update:{}
        //todo:翻页
        //todo:点击补全的命令
        Text.Builder builder = Text.builder();
        for(Map.Entry<ItemType,List<CheckRule>> entry:toShow.entrySet()){
            builder.append(getMessage("itemTypeLine","item_type",entry.getKey().getId()),Text.NEW_LINE);
            if(entry.getValue().size()>0){
                for(CheckRule checkRule:entry.getValue()){
                    builder.append(checkRule.toText(),Text.NEW_LINE);
                }
            }else {
                builder.append(getMessage("noRule"),Text.NEW_LINE);
            }
        }
        src.sendMessage(builder.build());
        return CommandResult.success();
    }
}
