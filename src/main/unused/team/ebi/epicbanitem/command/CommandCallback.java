package team.ebi.epicbanitem.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author The EpicBanItem Team
 */
@Singleton
@NonnullByDefault
public class CommandCallback extends AbstractCommand {
    /**
     * player uuid -> key-callback map
     */
    private static final Cache<UUID, Map<String, Tuple<CommandElement, CommandExecutor>>> callbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build();

    private static final Random RANDOM = new Random();

    public CommandCallback() {
        super("callback", "cb");
    }

    public static void clear(UUID player) {
        callbacks.invalidate(player);
    }

    public static ClickAction.RunCommand addCallback(UUID player, Consumer<CommandSource> consumer) {
        String key = add(
            player,
            GenericArguments.none(),
            (src, args) -> {
                consumer.accept(src);
                return CommandResult.success();
            }
        );
        return TextActions.runCommand(String.format("/%s cb %s", CommandEbi.COMMAND_PREFIX, key));
    }

    public static String add(UUID player, CommandElement element, CommandExecutor executor) {
        return add(player, new Tuple<>(element, executor));
    }

    public static String add(UUID player, Tuple<CommandElement, CommandExecutor> callback) {
        Map<String, Tuple<CommandElement, CommandExecutor>> map = callbacks.get(player, u -> new HashMap<>());
        //noinspection ConstantConditions
        String key = randomString(map.keySet());
        map.put(key, callback);
        return key;
    }

    private static String randomString(Set<String> set) {
        String s = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 4;
        while (true) {
            for (int i = 0; i < 100; i++) {
                char[] chars = new char[length];
                for (int j = 0; j < length; j++) {
                    chars[j] = s.charAt(RANDOM.nextInt(s.length()));
                }
                String r = new String(chars);
                if (!set.contains(r)) {
                    return r;
                }
            }
            length++;
        }
    }

    @Override
    public String getRootPermission() {
        return "epicbanitem.command." + parent + "editor";
    }

    @Override
    public CommandElement getArgument() {
        return new Arg(Text.of("args"));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        //noinspection OptionalGetWithoutIsPresent
        Tuple<CommandElement, CommandExecutor> callback = args.<Tuple<CommandElement, CommandExecutor>>getOne("args").get();
        return callback.getSecond().execute(src, args);
    }

    private final class Arg extends CommandElement {

        Arg(Text key) {
            super(Objects.requireNonNull(key));
        }

        @Override
        public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
            Tuple<CommandElement, CommandExecutor> val = parseValue(source, args);
            String key = getUntranslatedKey();
            //noinspection ConstantConditions
            context.putArg(key, val);
            try {
                val.getFirst().parse(source, args, context);
            } catch (ArgumentParseException e) {
                source.sendMessage(getMessage("neededArgs", Tuple.of("help", val.getFirst().getUsage(source))));
                throw e;
            }
        }

        @Nonnull
        @Override
        protected Tuple<CommandElement, CommandExecutor> parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            if (!(source instanceof Identifiable)) {
                throw args.createError(getMessage("unsupportedSource"));
            }
            String key = args.next();
            Map<String, Tuple<CommandElement, CommandExecutor>> map = callbacks.getIfPresent(((Identifiable) source).getUniqueId());
            if (map == null) {
                throw args.createError(getMessage("illegalKey"));
            }
            Tuple<CommandElement, CommandExecutor> callback = map.get(key);
            if (callback == null) {
                throw args.createError(getMessage("illegalKey"));
            }
            return callback;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                Tuple<CommandElement, CommandExecutor> callback = parseValue(src, args);
                return callback.getFirst().complete(src, args, context);
            } catch (ArgumentParseException e) {
                return Collections.emptyList();
            }
        }
    }
}
