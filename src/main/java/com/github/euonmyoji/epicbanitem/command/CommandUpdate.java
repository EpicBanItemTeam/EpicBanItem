package com.github.euonmyoji.epicbanitem.command;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.NbtTagDataUtil;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.QueryResult;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateExpression;
import com.github.euonmyoji.epicbanitem.util.nbt.UpdateResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;
import java.util.UUID;

import static org.spongepowered.api.command.args.GenericArguments.remainingRawJoinedStrings;

/**
 * @author ustc_zzzz
 */
@NonnullByDefault
public class CommandUpdate extends AbstractCommand {
    CommandUpdate() {
        super("update", "u");
    }

    @Override
    public CommandElement getArgument() {
        return GenericArguments.firstParsing(remainingRawJoinedStrings(Text.of("update-rule")));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof ArmorEquipable)) {
            throw new CommandException(Text.of("找不到物品。可能因为你不是玩家，或者手上没拿东西？"));
        }
        Optional<Tuple<HandType, ItemStack>> handItem = CommandCreate.getItemInHand(src);
        if (!handItem.isPresent()) {
            throw new CommandException(Text.of("找不到物品。可能因为你不是玩家，或者手上没拿东西？"));
        }
        UUID uuid = ((ArmorEquipable) src).getUniqueId();
        int quantity = handItem.get().getSecond().getQuantity();
        DataContainer nbt = NbtTagDataUtil.toNbt(handItem.get().getSecond());
        // noinspection ConstantConditions
        String updateRule = args.<String>getOne("update-rule").get();
        String queryRule = CommandQuery.histories.getOrDefault(uuid, "{}");
        try {
            UpdateExpression update = new UpdateExpression(TextUtil.serializeStringToConfigNode(updateRule));
            QueryExpression query = new QueryExpression(TextUtil.serializeStringToConfigNode(queryRule));
            // noinspection ConstantConditions
            QueryResult queryResult = query.query(DataQuery.of(), nbt).get();
            UpdateResult updateResult = update.update(queryResult, nbt);

            updateResult.apply(nbt);
            LiteralText text = Text.of(updateResult.toString());
            Text.Builder prefix = Text.builder("成功应用规则: ").onHover(TextActions.showText(text));
            src.sendMessage(Text.of(prefix.build(), TextUtil.serializeNbtToString(nbt, queryResult)));
        } catch (Exception e) {
            EpicBanItem.logger.error("应用规则时出错: ", e);
            throw new CommandException(Text.of("应用规则时出错: ", e.toString()));
        }
        try {
            ((ArmorEquipable) src).setItemInHand(handItem.get().getFirst(), NbtTagDataUtil.toItemStack(nbt, quantity));
            src.sendMessage(Text.of("成功应用物品。"));
        } catch (Exception e) {
            EpicBanItem.logger.error("应用物品时出错: ", e);
            throw new CommandException(Text.of("应用物品时出错: ", e.toString()));
        }
        return CommandResult.success();
    }
}
