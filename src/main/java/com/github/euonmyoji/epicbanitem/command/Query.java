package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.util.nbt.DataPredicate;
import com.github.euonmyoji.epicbanitem.util.nbt.NbtTypeHelper;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.util.Map;
import java.util.Optional;

class Query {

    private static BufferedReader reader;

    private static BufferedWriter writer;

    private static final ConfigurationLoader<CommentedConfigurationNode> LOADER = HoconConfigurationLoader.builder()
            .setSink(() -> writer)
            .setSource(() -> reader)
            .setParseOptions(getParseOptions())
            .setRenderOptions(getRenderOptions()).build();

    static CommandSpec query = CommandSpec.builder()
            .arguments(GenericArguments.remainingRawJoinedStrings(Text.of("query-rule")))
            .permission("epicbanitem.query")
            .executor(Query::execute)
            .build();

    private static CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<ItemStack> itemStackOptional = Optional.empty();
        if (src instanceof ArmorEquipable) {
            for (HandType type : Sponge.getRegistry().getAllOf(HandType.class)) {
                itemStackOptional = ((ArmorEquipable) src).getItemInHand(type);
                if (itemStackOptional.isPresent()) {
                    break;
                }
            }
        }
        if (!itemStackOptional.isPresent()) {
            throw new CommandException(Text.of("找不到物品。可能因为你不是玩家，或者手上没拿东西？"));
        }
        DataContainer nbt = NbtTypeHelper.toNbt(itemStackOptional.get());
        // noinspection ConstantConditions
        String rule = args.<String>getOne("query-rule").get();
        try {
            QueryExpression query = new QueryExpression(getFrom(rule));
            Optional<Map<String, DataPredicate>> result = query.testAndGetArrayPlaceholderFinder(DataQuery.of(), nbt);
            if (result.isPresent()) {
                src.sendMessage(Text.of("成功匹配物品: ", getFrom(DataTranslators.CONFIGURATION_NODE.translate(nbt))));
            } else {
                src.sendMessage(Text.of("未成功匹配物品。"));
            }
        } catch (Exception e) {
            throw new CommandException(Text.of("解析匹配时出错: ", e.getLocalizedMessage()));
        }
        return CommandResult.success();
    }

    private static ConfigurationNode getFrom(String string) throws IOException {
        try (StringReader in = new StringReader(string); BufferedReader bufferedReader = new BufferedReader(in)) {
            reader = bufferedReader;
            return LOADER.load();
        }
    }

    private static String getFrom(ConfigurationNode configNode) throws IOException {
        try (StringWriter out = new StringWriter(); BufferedWriter bufferedWriter = new BufferedWriter(out)) {
            writer = bufferedWriter;
            LOADER.save(configNode);
            return out.toString();
        }
    }

    private static ConfigParseOptions getParseOptions() {
        return ConfigParseOptions.defaults().setAllowMissing(true);
    }

    private static ConfigRenderOptions getRenderOptions() {
        return ConfigRenderOptions.concise().setFormatted(true);
    }
}
