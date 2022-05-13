package team.ebi.epicbanitem;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Predicates;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import team.ebi.epicbanitem.api.RestrictionPreset;
import team.ebi.epicbanitem.api.RestrictionPresets;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRules;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;
import team.ebi.epicbanitem.api.expression.ExpressionService;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;
import team.ebi.epicbanitem.util.DataSerializableValueParser;

@Singleton
public final class EBICommand {
  private static final class Parameters {
    public static final Parameter.Value.Builder<ResourceKey> RULE_NAME =
        Parameter.builder(ResourceKey.class)
            .addParser(
                new ValueParser<>() {
                  @Override
                  public Optional<? extends ResourceKey> parseValue(
                      Key<? super ResourceKey> parameterKey, Mutable reader, Builder context)
                      throws ArgumentParseException {
                    ResourceKey key = reader.parseResourceKey(EpicBanItem.NAMESPACE);
                    if (!key.namespace().equals(EpicBanItem.NAMESPACE))
                      throw new ArgumentParseException(
                          Component.translatable("epicbanitem.command.create.rejectNamespace"),
                          key.toString(),
                          key.namespace().length());
                    return Optional.of(key);
                  }

                  @Override
                  public List<ClientCompletionType> clientCompletionType() {
                    return List.of(ClientCompletionTypes.RESOURCE_KEY.get());
                  }
                });
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
                        .filter(it -> new StartsWithPredicate(it).test(currentInput))
                        .map(CommandCompletion::of)
                        .collect(Collectors.toList()));

    public static final Parameter.Value.Builder<RestrictionPreset> PRESET =
        Parameter.registryElement(
            TypeToken.get(RestrictionPreset.class), EBIRegistries.PRESET, EpicBanItem.NAMESPACE);

    public static final Parameter.Value.Builder<RestrictionRule> RULE =
        Parameter.builder(Keys.RULE)
            .addParser(new DataSerializableValueParser<>(RestrictionRuleImpl.class));

    public static final Parameter.Value.Builder<RootQueryExpression> QUERY =
        Parameter.builder(Keys.QUERY)
            .addParser(new DataSerializableValueParser<>(RootQueryExpression.class));

    public static final Parameter.Value.Builder<RootUpdateExpression> UPDATE =
        Parameter.builder(Keys.UPDATE)
            .addParser(new DataSerializableValueParser<>(RootUpdateExpression.class));

    public static final class Keys {
      public static final Parameter.Key<RootQueryExpression> QUERY =
          Parameter.key("query", RootQueryExpression.class);
      public static final Parameter.Key<RootUpdateExpression> UPDATE =
          Parameter.key("update", RootUpdateExpression.class);
      public static final Parameter.Key<ResourceKey> RULE_KEY =
          Parameter.key("rule-key", ResourceKey.class);

      public static final Parameter.Key<ResourceKey> RULE_NAME =
          Parameter.key("rule-name", ResourceKey.class);
      public static final Parameter.Key<RestrictionRule> RULE =
          Parameter.key("rule", RestrictionRule.class);
      public static final Parameter.Key<RestrictionPreset> PRESET =
          Parameter.key("preset", RestrictionPreset.class);
    }
  }

  private static final class Flags {
    public static final Flag BLOCK = Flag.builder().aliases("block", "b").build();

    public static final Flag PRESET =
        Flag.builder()
            .aliases("preset", "p")
            .setParameter(Parameters.PRESET.key(Parameters.Keys.PRESET).build())
            .build();
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

  private static Optional<LocatableBlock> targetLocation(Living living) {
    return RayTrace.block()
        .select(RayTrace.nonAir())
        .limit(5)
        .sourceEyePosition(living)
        .direction(living)
        .execute()
        .map(RayTraceResult::selectedObject);
  }

  private static Optional<BlockSnapshot> targetBlock(Living living) {
    return targetLocation(living).map(it -> it.serverLocation().createSnapshot());
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

  @Inject private ExpressionService expressionService;

  EBICommand() {}

  public Command.Parameterized build() {
    final Command.Parameterized query =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.query.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.query.description.extended"))
            .permission(EpicBanItem.permission("command.query"))
            .addFlag(Flags.BLOCK)
            .addParameter(Parameters.QUERY.optional().key(Parameters.Keys.QUERY).build())
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

    final Command.Parameterized create =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.create.description"))
            .permission(EpicBanItem.permission("command.create"))
            .addFlags(Flags.PRESET, Flags.BLOCK)
            .addParameters(
                Parameters.RULE_NAME.key(Parameters.Keys.RULE_NAME).build(),
                Parameters.QUERY.key(Parameters.Keys.QUERY).optional().build())
            .executor(this::create)
            .build();

    return Command.builder()
        .shortDescription(Component.translatable("command.root.description"))
        .permission(EpicBanItem.permission("command.root"))
        .addChild(query, "query")
        .addChild(remove, "remove", "rm")
        .addChild(update, "update")
        .addChild(create, "create")
        .build();
  }

  private @NotNull CommandResult remove(final CommandContext context) {
    ResourceKey key = context.requireOne(Parameters.Keys.RULE_KEY);
    String stringKey = key.asString();
    RestrictionRule rule = RestrictionRules.remove(key);
    if (Objects.isNull(rule)) {
      TextComponent.Builder builder = Component.text();
      builder.append(Component.translatable("epicbanitem.command.remove.notExist"));
      //noinspection deprecation
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

  private final Cache<UUID, RootQueryExpression> usedQuery =
      Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

  private @NotNull CommandResult query(final CommandContext context) throws CommandException {
    if (!(context.cause().root() instanceof Player))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED));
    final Player player = (Player) context.cause().root();
    boolean isBlock = context.hasFlag("block");
    UUID uuid = player.identity().uuid();
    // Argument > Last > Empty
    RootQueryExpression expression =
        context
            .one(Parameters.Keys.QUERY)
            .orElse(Objects.requireNonNull(usedQuery.get(uuid, it -> new RootQueryExpression())));
    usedQuery.put(uuid, expression);
    SerializableDataHolder targetObject =
        targetObject(player, isBlock)
            .orElseThrow(
                () ->
                    new CommandException(
                        isBlock
                            ? Component.translatable("epicbanitem.command.needBlock")
                            : Component.translatable("epicbanitem.command.needItem")));
    DataView container = ExpressionService.cleanup(targetObject.toContainer());
    Optional<QueryResult> result = expression.query(container);
    Sponge.serviceProvider()
        .paginationService()
        .builder()
        .title(objectName(targetObject))
        .header(
            result.isPresent() ? Component.translatable("epicbanitem.command.query.success") : null)
        .contents(expressionService.renderQueryResult(container, result.orElse(null)))
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
        Objects.requireNonNull(usedQuery.get(uuid, it -> new RootQueryExpression()));
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
    DataView cleaned = ExpressionService.cleanup(container);
    QueryResult result = queryExpression.query(cleaned).orElse(QueryResult.success());
    UpdateOperation operation = updateExpression.update(result, cleaned);
    DataView processed = operation.process(cleaned);
    processed
        .values(false)
        .forEach(
            (query, o) -> {
              container.remove(query);
              container.set(query, o);
            });
    DataManager dataManager = Sponge.dataManager();
    ItemStack deserialized = dataManager.deserialize(ItemStack.class, container).orElseThrow();
    if (deserialized.quantity() > deserialized.maxStackQuantity())
      deserialized.setQuantity(deserialized.maxStackQuantity());
    if (isBlock) {
      BlockType blockType = deserialized.type().block().orElseThrow();
      BlockState oldState = block.state();
      BlockState newState =
          BlockState.builder()
              .blockType(blockType)
              .addFrom(blockType.defaultState())
              .addFrom(oldState)
              .build();
      BlockSnapshot newSnapshot = BlockSnapshot.builder().from(block).blockState(newState).build();
      newSnapshot.restore(true, BlockChangeFlags.DEFAULT_PLACEMENT);
    } else player.equip(hand, deserialized);
    player.sendMessage(operation.asComponent());
    return CommandResult.success();
  }

  private CommandResult create(CommandContext context) {
    if (!(context.cause().root() instanceof Player))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED));
    final Player player = (Player) context.cause().root();
    final RestrictionPreset preset =
        context.one(Parameters.Keys.PRESET).orElse(RestrictionPresets.TYPE.get());
    final ResourceKey name = context.requireOne(Parameters.Keys.RULE_NAME);
    final Optional<RootQueryExpression> expression = context.one(Parameters.Keys.QUERY);
    DataContainer expressionView = DataContainer.createNew();
    final boolean isBlock = context.hasFlag(Flags.BLOCK);
    expression.ifPresent(it -> it.view().values(false).forEach(expressionView::set));
    if (isBlock) {
      Optional<BlockSnapshot> block = targetBlock(player);
      List<DataView> views =
          block
              .flatMap(it -> it.location().flatMap(Location::blockEntity))
              .filter(Predicates.instanceOf(CarrierBlockEntity.class))
              .map(it -> ((CarrierBlockEntity) it).inventory())
              .filter(it -> !it.peek().isEmpty())
              .map(it -> it.slots().stream())
              .stream()
              .flatMap(Function.identity())
              .map(Inventory::peek)
              .filter(Predicate.not(ItemStack::isEmpty))
              .map(DataSerializable::toContainer)
              .map(ExpressionService::cleanup)
              .map(preset)
              .filter(it -> !it.keys(false).isEmpty())
              .collect(Collectors.toList());
      if (!views.isEmpty()) {
        expressionView.set(ExpressionQueries.OR, views);
      } else {
        block
            .map(it -> ItemStack.builder().fromBlockSnapshot(it).build())
            .map(DataSerializable::toContainer)
            .map(ExpressionService::cleanup)
            .map(preset)
            .ifPresent(view -> view.values(false).forEach(expressionView::set));
      }
    } else {
      heldHand(player)
          .flatMap(it -> equipped(player, it))
          .map(DataSerializable::toContainer)
          .map(ExpressionService::cleanup)
          .map(preset)
          .ifPresent(view -> view.values(false).forEach(expressionView::set));
    }
    if (expressionView.keys(false).isEmpty()) {
      return CommandResult.error(Component.translatable("epicbanitem.command.create.noExpression"));
    }
    player.sendMessage(
        Component.text(new Gson().toJson(expressionView.getMap(DataQuery.of()).get())));
    return CommandResult.success();
  }
}
