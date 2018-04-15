package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.github.euonmyoji.epicbanitem.check.CheckRuleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EpicBanItemArgs {

    public static CommandElement itemOrHand(Text key,boolean explicitHand){
        return new ArgItemOrHand(key,explicitHand);
    }

    public static class ArgItemOrHand extends CommandElement {
        private boolean explicitHand;

        private ArgItemOrHand(@Nullable Text key, boolean explicitHand) {
            super(key);
            this.explicitHand = explicitHand;
        }

        @Override
        protected ItemType parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            boolean isPlayer = source instanceof Player;
            if(!args.hasNext()){
                if(isPlayer && !explicitHand){
                    return getItemTypeFormHand((Player) source);
                }else {
                    //todo:消息提示
                    throw args.createError(Text.EMPTY);
                }
            }
            String argString = args.next();
            if(isPlayer && explicitHand && argString.equalsIgnoreCase("hand")){
                return getItemTypeFormHand((Player) source);
            }
            Optional<ItemType> optionalItemType = Sponge.getRegistry().getType(ItemType.class,argString);
            if(optionalItemType.isPresent()){
                return optionalItemType.get();
            }else if(isPlayer && !explicitHand){
                return getItemTypeFormHand((Player) source);
            }else {
                //todo:消息提示
                throw args.createError(Text.EMPTY);
            }
        }

        //todo:手持空气？
        private ItemType getItemTypeFormHand(Player player){
            return player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty()).getType();
        }

        @Override
        public java.util.List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            String prefix = args.nextIfPresent().orElse("").toLowerCase();
            List<String> list = Sponge.getRegistry().getAllOf(ItemType.class).stream().map(ItemType::getId).filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
            if(explicitHand && "hand".startsWith(prefix)){
                list.add("hand");
            }
            return list;
        }


        @Override
        public Text getUsage(CommandSource src) {
            if(src instanceof Player) {
                if (explicitHand) {
                    return getKey() == null ? Text.of() : Text.of("<hand|", getKey(), ">");
                } else {
                    return getKey() == null ? Text.of() : Text.of("[", getKey(), "]");
                }
            }else {
                return super.getUsage(src);
            }
        }
    }

    public static CommandElement checkRule(Text key){
        return checkRule(key,false);
    }

    public static CommandElement checkRule(Text key, boolean explicitHand){
        return GenericArguments.seq(
                itemOrHand(Text.of("item-type"),explicitHand),
                new ArgCheckRule(key)
        );
    }

    public static class ArgCheckRule extends CommandElement {

        private ArgCheckRule(@Nullable Text key) {
            super(key);
        }

        @Override
        public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
            Object val = parseValue(source, args);
            String key = getUntranslatedKey();
            ItemType itemType = context.<ItemType>getOne("item-type").get();
            String argString = args.next();
            CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
            CheckRule rule = null;
            for (CheckRule rule1:service.getCheckRules(itemType)){
                if(rule1.getName().equalsIgnoreCase(argString)){
                    rule = rule1;
                    break;
                }
            }
            if(rule != null){
                context.putArg(getKey(),rule);
            }else {
                //todo:提示信息
                throw args.createError(Text.EMPTY);
            }
        }

        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            ItemType itemType = context.<ItemType>getOne("item-type").get();
            String prefix = args.nextIfPresent().orElse("").toLowerCase();
            CheckRuleService service = Sponge.getServiceManager().provideUnchecked(CheckRuleService.class);
            return service.getCheckRules(itemType).stream().map(CheckRule::getName).filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
        }
    }
}
