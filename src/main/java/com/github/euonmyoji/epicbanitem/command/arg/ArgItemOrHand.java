package com.github.euonmyoji.epicbanitem.command.arg;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NonnullByDefault
class ArgItemOrHand extends CommandElement {
    private boolean explicitHand;

    ArgItemOrHand(@Nullable Text key, boolean explicitHand) {
        super(key);
        this.explicitHand = explicitHand;
    }

    @Override
    protected ItemType parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        boolean isPlayer = source instanceof Player;
        if (!args.hasNext()) {
            if (isPlayer && !explicitHand) {
                return getItemTypeFormHand((Player) source);
            } else {
                //todo:消息提示
                throw args.createError(Text.EMPTY);
            }
        }
        String argString = args.next();
        if (isPlayer && explicitHand && argString.equalsIgnoreCase("hand")) {
            return getItemTypeFormHand((Player) source);
        }
        Optional<ItemType> optionalItemType = Sponge.getRegistry().getType(ItemType.class, argString);
        if (optionalItemType.isPresent()) {
            return optionalItemType.get();
        } else if (isPlayer && !explicitHand) {
            return getItemTypeFormHand((Player) source);
        } else {
            //todo:消息提示
            throw args.createError(Text.EMPTY);
        }
    }

    //todo:手持空气？
    private ItemType getItemTypeFormHand(Player player) {
        return player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty()).getType();
    }

    @Override
    public java.util.List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String prefix = args.nextIfPresent().orElse("").toLowerCase();
        List<String> list = Sponge.getRegistry().getAllOf(ItemType.class).stream().map(ItemType::getId).filter(s -> s.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
        if (explicitHand && "hand".startsWith(prefix)) {
            list.add("hand");
        }
        return list;
    }


    @Override
    public Text getUsage(CommandSource src) {
        if (src instanceof Player) {
            if (explicitHand) {
                return getKey() == null ? Text.of() : Text.of("<hand|", getKey(), ">");
            } else {
                return getKey() == null ? Text.of() : Text.of("[", getKey(), "]");
            }
        } else {
            return super.getUsage(src);
        }
    }
}