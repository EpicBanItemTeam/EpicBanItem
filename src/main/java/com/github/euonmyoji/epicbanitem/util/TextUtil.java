package com.github.euonmyoji.epicbanitem.util;

import com.github.euonmyoji.epicbanitem.util.nbt.NbtTagRenderer;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import com.google.gson.stream.JsonWriter;
import com.typesafe.config.ConfigParseOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.spongepowered.api.text.Text.builder;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.action.TextActions.*;

/**
 * @author yinyangshi
 */
@SuppressWarnings("WeakerAccess")
public class TextUtil {

    /**
     * @param command  指令#点击后执行的 带/
     * @param describe 指令描述
     * @param hoverStr 覆盖时的text 如果没有就用默认的
     * @return 带gui的指令提示text
     */
    private static Text runCommandGui(String command, String describe, @Nullable String hoverStr) {
        return builder().append(of(TextStyles.UNDERLINE, command, TextStyles.RESET, " " + describe))
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
        return builder().append(of(TextStyles.UNDERLINE, command + " " + commandArgs, TextStyles.RESET, " " + describe))
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

    /**
     * @param origin origin string support FormatText
     * @param keySet placeholders in the string
     * @return TextTemplate
     */
    public static TextTemplate parseTextTemplate(String origin, Set<String> keySet) {
        boolean checkFirst = origin.startsWith("{");
        List<Object> objects = new ArrayList<>();
        String[] subStrings = origin.split("\\{");
        for (int i = 0; i < subStrings.length; i++) {
            String subString = subStrings[i];
            if (i == 0 && !checkFirst) {
                objects.add(parseFormatText(subString));
                continue;
            }
            String[] muSub = subString.split("}");
            if (muSub.length == 1 && subString.endsWith("}") && keySet.contains(muSub[0])) {
                objects.add(TextTemplate.arg(muSub[0]));
            } else if (muSub.length > 1 && keySet.contains(muSub[0])) {
                objects.add(TextTemplate.arg(muSub[0]));
                StringBuilder left = new StringBuilder(muSub[1]);
                for (int j = 2; j < muSub.length; j++) {
                    left.append("}");
                    left.append(muSub[j]);
                }
                if (subString.endsWith("}")) {
                    left.append("}");
                }
                objects.add(parseFormatText(left.toString()));
            } else {
                objects.add(parseFormatText("{" + subString));
            }
        }
        return TextTemplate.of(objects.toArray());
    }

    public static Text parseFormatText(String in) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(in);
    }


    public static Text serializeNbtToString(DataView nbt, QueryResult result) {
        return new NbtTagRenderer(result).render(nbt);
    }

    public static String escape(String unescapedString) {
        try (StringWriter out = new StringWriter()) {
            JsonWriter writer = new JsonWriter(out);
            writer.value(unescapedString).close();
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedReader delegationReader;
    private static BufferedWriter delegationWriter;

    private static final ConfigurationLoader<CommentedConfigurationNode> LOADER = HoconConfigurationLoader.builder()
            .setSource(() -> delegationReader).setSink(() -> delegationWriter)
            .setParseOptions(ConfigParseOptions.defaults().setAllowMissing(true)).build();

    public static ConfigurationNode serializeStringToConfigNode(String string) throws IOException {
        try (StringReader in = new StringReader(string); BufferedReader bufferedReader = new BufferedReader(in)) {
            delegationReader = bufferedReader;
            return LOADER.load();
        }
    }

    public static String deserializeConfigNodeToString(ConfigurationNode configNode) throws IOException {
        try (StringWriter out = new StringWriter(); BufferedWriter bufferedWriter = new BufferedWriter(out)) {
            delegationWriter = bufferedWriter;
            LOADER.save(configNode);
            return out.toString();
        }
    }
}
