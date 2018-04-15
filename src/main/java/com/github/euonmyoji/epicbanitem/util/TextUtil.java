package com.github.euonmyoji.epicbanitem.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.spongepowered.api.text.Text.builder;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.*;

public class TextUtil {

    /**
     * @param command  指令#点击后执行的 带/
     * @param describe 指令描述
     * @param hoverStr 覆盖时的text 如果没有就用默认的
     * @return 带gui的指令提示text
     */
    private static Text runCommandGui(String command, String describe, @Nullable String hoverStr) {
        return builder().append(of(command + " " + describe))
                .onClick(runCommand(command))
                .onHover(showText(of(hoverStr != null ? hoverStr : "点击执行" + command)))
                .build();
    }

    public static Text runCommandGui(String command, String describe) {
        return runCommandGui(command, describe, null);
    }

    /**
     * @param receiver 接受gui消息的人
     * @see TextUtil
     */
    public static void runCommandGuiThenSend(MessageReceiver receiver, String command, String describe, @Nullable String hoverStr) {
        receiver.sendMessage(runCommandGui(command, describe, hoverStr));
    }

    public static void runCommandGuiThenSend(MessageReceiver receiver, String command, String describe) {
        runCommandGuiThenSend(receiver, command, describe, null);
    }

    /**
     * @param command     指令#点击后显示的 带/
     * @param describe    指令描述
     * @param commandArgs 命令所需参数
     * @return 带gui的指令提示text
     */
    private static Text suggestCommandGui(String command, String describe, @Nonnull String commandArgs) {
        return builder().append(of(command + " " + commandArgs + " " + describe))
                .onClick(suggestCommand(command + " "))
                .onHover(showText(of(command + " " + commandArgs)))
                .build();
    }

    /**
     * @param receiver 接受gui消息的人
     * @see TextUtil
     */
    public static void suggestCommandGuiThenSend(MessageReceiver receiver, String command, String describe, @Nonnull String commandArgs) {
        receiver.sendMessage(suggestCommandGui(command, describe, commandArgs));
    }
}
