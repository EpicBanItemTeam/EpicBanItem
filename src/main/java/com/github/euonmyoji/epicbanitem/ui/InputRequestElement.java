package com.github.euonmyoji.epicbanitem.ui;

import com.github.euonmyoji.epicbanitem.command.CommandCallback;
import com.github.euonmyoji.epicbanitem.command.CommandEbi;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

public class InputRequestElement<T> implements UiTextElement {
    private Supplier<Text.Builder> display;
    private Supplier<?> suggestion;
    private CommandElement element;
    private CommandExecutor executor;

    public InputRequestElement(Supplier<Text.Builder> display, Supplier<?> suggestion, Consumer<T> update, CommandElement element) {
        this(
            display,
            suggestion,
            element,
            (src, args) -> {
                update.accept(args.<T>getOne(element.getKey()).orElseThrow(NoSuchElementException::new));
                return CommandResult.success();
            }
        );
    }

    public InputRequestElement(Supplier<Text.Builder> display, Supplier<?> suggestion, CommandElement element, CommandExecutor executor) {
        this.display = display;
        this.suggestion = suggestion;
        this.element = element;
        this.executor = executor;
    }

    @Override
    public Text toText(Player viewer) {
        String key = CommandCallback.add(viewer.getUniqueId(), element, executor);
        String commandString = String.format("/%s cb %s %s", CommandEbi.COMMAND_PREFIX, key, suggestion.get());
        return display.get().onClick(TextActions.suggestCommand(commandString)).build();
    }
}
