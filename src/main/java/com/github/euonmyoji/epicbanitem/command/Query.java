package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.remainingRawJoinedStrings;

class Query {
    static Map<UUID, String> histories = new HashMap<>();

    static CommandSpec query = CommandSpec.builder()
            .arguments(optional(remainingRawJoinedStrings(Text.of("query-rule"))))
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
        UUID uuid = ((ArmorEquipable) src).getUniqueId();
        DataContainer nbt = NbtTagDataUtil.toNbt(itemStackOptional.get());
        // noinspection ConstantConditions
        String rule = args.<String>getOne("query-rule").orElse(histories.getOrDefault(uuid, "{}"));
        try {
            QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(rule));
            Optional<QueryResult> result = query.query(DataQuery.of(), nbt);
            if (result.isPresent()) {
                LiteralText text = Text.of(result.get().toString());
                Text.Builder prefix = Text.builder("成功匹配物品: ").onHover(TextActions.showText(text));
                src.sendMessage(Text.of(prefix.build(), TextUtil.serializeNbtToString(nbt, result.get())));
            } else {
                src.sendMessage(Text.of("未成功匹配物品。"));
            }
            histories.put(uuid, rule);
        } catch (Exception e) {
            EpicBanItem.logger.error("解析匹配时出错: ",e);
            throw new CommandException(Text.of("解析匹配时出错: ", e.toString()));
        }
        return CommandResult.success();
    }
}
