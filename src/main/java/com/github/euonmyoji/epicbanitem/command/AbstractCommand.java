package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractCommand implements ICommand,CommandExecutor {

    protected CommandSpec commandSpec;

    protected String name;

    protected String[] alias;

    public AbstractCommand(String name,String... alias){
        this.name = name;
        this.alias = alias;
    }


    public String getRootPermission(){
        return "epicbanitem.command."+name;
    }

    protected String getPermission(String s){
        return getRootPermission()+"."+s;
    }

    protected String getMessageKey(String s){
        return "epicbanitem.command."+name+"."+s;
    }

    protected Text getMessage(String s){
        return EpicBanItem.plugin.getMessages().getMessage(getMessageKey(s));
    }

    protected Text getMessage(String s,String k1,Object v1){
        return EpicBanItem.plugin.getMessages().getMessage(getMessageKey(s),k1,v1);
    }

    public Text getDescription(){
        return getMessage("description");
    }

    public Text getExtendedDescription(){
        return getMessage("extendedDescription");
    }

    public Text getArgHelp(CommandSource source){
        return getMessage("argHelp");
    }

    public abstract CommandElement getArgument();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getAlias() {
        return alias;
    }

    @Override
    public CommandSpec getCallable() {
        if(commandSpec == null){
            Help help = new Help();
            commandSpec = CommandSpec.builder()
                    .permission(getRootPermission())
                    .description(getDescription())
                    .extendedDescription(getExtendedDescription())
                    .arguments(help)
                    .executor(help)
                    .build();
        }
        return commandSpec;
    }

    @NonnullByDefault
    private class Help extends CommandElement implements CommandExecutor{
        private CommandElement commandElement = getArgument();

        private Help() {
            super(Text.of("help"));
        }

        @Override
        public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
            Object state = args.getState();
            try {
                commandElement.parse(source,args,context);
            }catch (ArgumentParseException e){
                args.setState(state);
                if(args.next().equalsIgnoreCase("help")){
                    context.putArg("help",true);
                }else {
                    throw e;
                }
            }
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args){
            //do nothing here
            return null;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                if(!args.hasNext()||"help".startsWith(args.peek().toLowerCase())){
                    List<String> stringList = new ArrayList<>();
                    stringList.add("help");
                    stringList.addAll(getCallable().complete(src,args,context));
                    return stringList;
                }else {
                    return getCallable().complete(src,args,context);
                }
            } catch (ArgumentParseException e) {
                e.printStackTrace();
                return getCallable().complete(src,args,context);
            }
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if(args.hasAny("help")){
                //todo:使用翻译 , 颜色
                Text.Builder builder = Text.builder();
                builder.append(Text.of("Command:",getName()),Text.NEW_LINE);
                builder.append(getDescription(),Text.NEW_LINE);
                if(getAlias().length>0){
                    builder.append(Text.of("Alias:"));
                    for(String alias:getAlias()){
                        builder.append(Text.of(alias," "));
                    }
                    builder.append(Text.NEW_LINE);
                }
                builder.append(Text.of("Usages:"),getUsage(src),Text.NEW_LINE);
                builder.append(getArgHelp(src),Text.NEW_LINE);
//                builder.append(getExtendedDescription(),Text.NEW_LINE);
                return CommandResult.success();
            }else {
                return AbstractCommand.this.execute(src,args);
            }
        }

        @Override
        public Text getUsage(CommandSource src) {
            Text usage = commandElement.getUsage(src);
            if(usage.isEmpty()){
                return Text.of("help");
            }else {
                return Text.of("help|",usage);
            }
        }
    }

}
