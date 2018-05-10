package com.github.euonmyoji.epicbanitem.command;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public abstract class AbstractCommand implements ICommand,CommandExecutor {

    protected CommandSpec commandSpec;

    protected String name;

    protected String[] alias;

    public AbstractCommand(String name,String... alias){
        this.name = name;
        this.alias = alias;
    }


    public String getPermission(){
        return "epicbanitem.command."+name;
    }

    public Text getDescription(){
        //todo:翻译支持？
        return Text.EMPTY;
    }

    public Text getUsageHelp(CommandSource source){
        //todo:翻译支持？需要提供CommandSource么?
        return Text.EMPTY;
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
            commandSpec = CommandSpec.builder()
                    .permission(getPermission())
                    .description(getDescription())
                    .arguments(getArgument())
                    .executor(this)
                    .build();
        }
        return commandSpec;
    }

}
