package team.ebi.epicbanitem;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRules;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;
import team.ebi.epicbanitem.util.DataSerializableValueParser;
import team.ebi.epicbanitem.util.DataViewUtils;
import team.ebi.epicbanitem.util.QueryResultRenderer;

public final class EBICommand {
  private static final Cache<UUID, QueryExpression> USED_QUERY =
      Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

  private static final class Parameters {
    public static final Parameter.Value.Builder<ResourceKey> RULE_KEY =
        Parameter.resourceKey()
            .completer(
                (context, currentInput) ->
                    RestrictionRules.keyStream()
                        .map(
                            it ->
                                it.namespace().equals(EpicBanItem.NAMESPACE)
                                    ? it.value()
                                    : it.asString())
                        .filter(it -> it.startsWith(currentInput))
                        .map(CommandCompletion::of)
                        .collect(Collectors.toList()));

    public static final Parameter.Value.Builder<RestrictionRule> RULE =
        Parameter.builder(RestrictionRule.class)
            .addParser(
                (key, reader, context) ->
                    Optional.ofNullable(
                        RestrictionRules.get(reader.parseResourceKey(EpicBanItem.NAMESPACE))))
            .completer(
                (context, currentInput) ->
                    RestrictionRules.keyStream()
                        .map(
                            it ->
                                it.namespace().equals(EpicBanItem.NAMESPACE)
                                    ? it.value()
                                    : it.asString())
                        .filter(it -> it.startsWith(currentInput))
                        .map(CommandCompletion::of)
                        .collect(Collectors.toList()));

    public static final Parameter.Value.Builder<QueryExpression> QUERY =
        Parameter.builder(QueryExpression.class)
            .addParser(new DataSerializableValueParser<>(RootQueryExpression.class))
            .optional();

    public static final Parameter.Value.Builder<UpdateExpression> UPDATE =
        Parameter.builder(UpdateExpression.class)
            .addParser(new DataSerializableValueParser<>(RootUpdateExpression.class))
            .optional();

    public static final class Keys {
      public static final Parameter.Key<QueryExpression> QUERY =
          Parameter.key("query", QueryExpression.class);
      public static final Parameter.Key<UpdateExpression> UPDATE =
          Parameter.key("update", UpdateExpression.class);
      private static final Parameter.Key<ResourceKey> RULE_KEY =
          Parameter.key("rule-key", ResourceKey.class);
    }
  }

  private static final class Flags {
    public static final Flag BLOCK = Flag.builder().aliases("block", "b").build();
  }

  @Inject
  EBICommand(PluginContainer plugin, EventManager eventManager) {
    eventManager.registerListener(
        EventListenerRegistration.builder(new TypeToken<RegisterCommandEvent<Parameterized>>() {})
            .plugin(plugin)
            .listener(event -> event.register(plugin, build(), "epicbanitem", "ebi"))
            .order(Order.DEFAULT)
            .build());
  }

  public Command.Parameterized build() {
    final Command.Parameterized query =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.query.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.query.description.extended"))
            .permission(EpicBanItem.permission("command.query"))
            .addFlag(Flags.BLOCK)
            .addParameter(Parameters.QUERY.key(Parameters.Keys.QUERY).build())
            .executor(this::query)
            .build();

    final Command.Parameterized remove =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.remove.description"))
            .permission(EpicBanItem.permission("command.remove"))
            .addParameter(Parameters.RULE_KEY.key(Parameters.Keys.RULE_KEY).build())
            .executor(this::remove)
            .build();

    final Command.Parameterized update =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.update.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.update.description.extended"))
            .permission(EpicBanItem.permission("command.update"))
            .addFlag(Flags.BLOCK)
            .addParameter(Parameters.UPDATE.key(Parameters.Keys.UPDATE).build())
            .executor(this::update)
            .build();

    return Command.builder()
        .shortDescription(Component.translatable("command.root.description"))
        .permission(EpicBanItem.permission("command.root"))
        .addChild(query, "query")
        .addChild(remove, "remove", "rm")
        .addChild(update, "update")
        .build();
  }

  private @NotNull CommandResult remove(final CommandContext context) {
    ResourceKey key = context.requireOne(Parameters.Keys.RULE_KEY);
    String stringKey = key.asString();
    RestrictionRule rule = RestrictionRules.remove(key);
    if (Objects.isNull(rule)) {
      TextComponent.Builder builder = Component.text();
      builder.append(Component.translatable("epicbanitem.command.remove.notExist"));
      RestrictionRules.keyStream()
          .map(ResourceKey::asString)
          .min(Comparator.comparingInt(k -> StringUtils.getLevenshteinDistance(k, stringKey)))
          .map(
              it ->
                  Component.translatable("epicbanitem.command.suggestRule")
                      .args(Component.text(it).clickEvent(ClickEvent.copyToClipboard(it))))
          .ifPresent(it -> builder.append(Component.newline()).append(it));
      return CommandResult.error(builder.build());
    }
    return CommandResult.success();
  }

  private @NotNull CommandResult query(final CommandContext context) throws CommandException {
    if (!(context.cause().root() instanceof Player))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED));
    final Player player = (Player) context.cause().root();
    boolean isBlock = context.hasFlag("block");
    UUID uuid = player.identity().uuid();
    // Argument > Last > Empty
    QueryExpression expression =
        context
            .one(Parameters.Keys.QUERY)
            .orElse(
                Objects.requireNonNull(
                    USED_QUERY.get(
                        uuid,
                        it -> new RootQueryExpression(DataContainer.createNew(), DataQuery.of()))));
    USED_QUERY.put(uuid, expression);
    SerializableDataHolder targetObject =
        targetObject(player, isBlock)
            .orElseThrow(
                () ->
                    new CommandException(
                        isBlock
                            ? Component.translatable("epicbanitem.command.needBlock")
                            : Component.translatable("epicbanitem.command.needItem")));
    DataView container = DataViewUtils.cleanup(targetObject.toContainer());
    Optional<QueryResult> result = expression.query(container);
    Sponge.serviceProvider()
        .paginationService()
        .builder()
        .title(objectName(targetObject))
        .header(
            result.isPresent() ? Component.translatable("epicbanitem.command.query.success") : null)
        .contents(QueryResultRenderer.render(container, result.orElse(null)))
        .sendTo(context.cause().audience());
    return CommandResult.success();
  }

  private @NotNull CommandResult update(final CommandContext context) throws CommandException {
    if (!(context.cause().root() instanceof Player))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED));
    final Player player = (Player) context.cause().root();
    boolean isBlock = context.hasFlag("block");
    UUID uuid = player.identity().uuid();
    // Last > Empty
    QueryExpression queryExpression =
        Objects.requireNonNull(
            USED_QUERY.get(
                uuid, it -> new RootQueryExpression(DataContainer.createNew(), DataQuery.of())));
    UpdateExpression updateExpression = context.requireOne(Parameters.Keys.UPDATE);
    ItemStack targetObject;
    EquipmentType hand = null;
    BlockSnapshot block = null;
    if (isBlock) {
      block =
          targetBlock(player)
              .orElseThrow(
                  () ->
                      new CommandException(
                          Component.translatable("epicbanitem.command.needBlock")));
      targetObject = ItemStack.builder().fromBlockSnapshot(block).build();
    } else {
      hand =
          heldHand(player)
              .orElseThrow(
                  () ->
                      new CommandException(Component.translatable("epicbanitem.command.needItem")));
      targetObject =
          equipped(player, hand)
              .orElseThrow(
                  () ->
                      new CommandException(Component.translatable("epicbanitem.command.needItem")));
    }
    DataContainer container = targetObject.toContainer();
    DataView cleaned = DataViewUtils.cleanup(container);
    QueryResult result = queryExpression.query(cleaned).orElse(QueryResult.success());
    UpdateOperation operation = updateExpression.update(result, cleaned);
    DataView processed = operation.process(cleaned);
    for (DataQuery key : processed.keys(false)) container.set(key, processed.get(key).get());
    DataManager dataManager = Sponge.dataManager();
    ItemStack deserialized = dataManager.deserialize(ItemStack.class, container).orElseThrow();
    if (isBlock) {
      BlockType blockType = deserialized.type().block().orElseThrow();
      BlockState oldState = block.state();
      BlockState newState =
          BlockState.builder()
              .blockType(blockType)
              .addFrom(blockType.defaultState())
              .addFrom(oldState)
              .build();
      BlockSnapshot newSnapshot =
          BlockSnapshot.builder()
              .from(block)
              .blockState(newState)
              .build();
      newSnapshot.location();
      newSnapshot.restore(true, BlockChangeFlags.DEFAULT_PLACEMENT);
    } else player.equip(hand, deserialized);
    player.sendMessage(operation.asComponent());
    return CommandResult.success();
  }

  private static Component objectName(SerializableDataHolder holder) {
    Component objectName =
        holder
            .get(Keys.DISPLAY_NAME)
            .orElseGet(
                () -> {
                  if (holder instanceof BlockSnapshot)
                    return ((BlockSnapshot) holder).state().type().asComponent();
                  else return ItemTypes.AIR.get().asComponent();
                });
    if (holder instanceof ItemStackSnapshot)
      objectName = objectName.hoverEvent((ItemStackSnapshot) holder);
    return objectName;
  }

  private static Optional<ItemStack> targetObject(Player player, boolean isBlock) {
    return Optional.of(isBlock)
        .filter(Boolean::booleanValue)
        .flatMap(
            ignored ->
                targetBlock(player).map(it -> ItemStack.builder().fromBlockSnapshot(it).build()))
        .or(() -> heldHand(player).flatMap(it -> equipped(player, it)));
  }

  private static Optional<BlockSnapshot> targetBlock(Living living) {
    return RayTrace.block()
        .select(RayTrace.nonAir())
        .limit(5)
        .sourceEyePosition(living)
        .direction(living)
        .execute()
        .map(it -> it.selectedObject().serverLocation().createSnapshot());
  }

  private static Optional<EquipmentType> heldHand(Equipable equipable) {
    return equipable
        .equipped(EquipmentTypes.MAIN_HAND.get())
        .filter(Predicate.not(ItemStack::isEmpty))
        .map(it -> EquipmentTypes.MAIN_HAND.get())
        .or(
            () ->
                equipable
                    .equipped(EquipmentTypes.OFF_HAND.get())
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .map(it -> EquipmentTypes.OFF_HAND.get()));
  }

  private static Optional<ItemStack> equipped(Equipable equipable, EquipmentType type) {
    return equipable.equipped(type).filter(Predicate.not(ItemStack::isEmpty));
  }
}
