package team.ebi.epicbanitem;

import static team.ebi.epicbanitem.util.DataUtils.objectName;
import static team.ebi.epicbanitem.util.EntityUtils.equipped;
import static team.ebi.epicbanitem.util.EntityUtils.heldHand;
import static team.ebi.epicbanitem.util.EntityUtils.targetBlock;
import static team.ebi.epicbanitem.util.EntityUtils.targetObject;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;
import team.ebi.epicbanitem.api.RestrictionPreset;
import team.ebi.epicbanitem.api.RestrictionPresets;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRuleService;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.RestrictionTrigger;
import team.ebi.epicbanitem.api.RestrictionTriggers;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;
import team.ebi.epicbanitem.api.expression.ExpressionService;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.expression.UpdateExpression;
import team.ebi.epicbanitem.api.expression.UpdateOperation;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;
import team.ebi.epicbanitem.util.Components;
import team.ebi.epicbanitem.util.command.Flags;
import team.ebi.epicbanitem.util.command.Parameters;

@Singleton
public final class EBICommands {
  @Inject private ExpressionService expressionService;
  @Inject private RestrictionRuleService ruleService;
  @Inject private RestrictionService restrictionService;
  @Inject private RulePredicateService predicateService;
  @Inject private Parameters parameters;
  @Inject private Parameters.Keys keys;
  @Inject private Flags flags;

  public Command.Parameterized build() {
    final Command.Parameterized query =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.query.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.query.description.extended"))
            .permission(EpicBanItem.permission("command.query"))
            .addFlag(flags.block)
            .addParameter(parameters.query.optional().key(keys.query).build())
            .executor(this::query)
            .build();

    final Command.Parameterized remove =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.remove.description"))
            .permission(EpicBanItem.permission("command.remove"))
            .addParameter(parameters.ruleKey.key(keys.ruleKey).build())
            .executor(this::remove)
            .build();

    final Command.Parameterized update =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.update.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.update.description.extended"))
            .permission(EpicBanItem.permission("command.update"))
            .addFlag(flags.block)
            .addParameter(parameters.update.key(keys.update).build())
            .executor(this::update)
            .build();

    final Command.Parameterized create =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.create.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.create.description.extended"))
            .permission(EpicBanItem.permission("command.create"))
            .addFlags(flags.preset, flags.block)
            .addParameters(
                parameters.ruleName.key(keys.ruleName).build(),
                parameters.query.key(keys.query).optional().build())
            .executor(this::create)
            .build();

    final Command.Parameterized test =
        Command.builder()
            .shortDescription(Component.translatable("epicbanitem.command.test.description"))
            .extendedDescription(
                Component.translatable("epicbanitem.command.test.description.extended"))
            .permission(EpicBanItem.permission("command.test"))
            .addFlags(flags.block, flags.trigger, flags.world)
            .executor(this::test)
            .build();

    return Command.builder()
        .shortDescription(Component.translatable("epicbanitem.command.root.description"))
        .permission(EpicBanItem.permission("command.root"))
        .addChild(query, "query")
        .addChild(remove, "remove", "rm")
        .addChild(update, "update")
        .addChild(create, "create")
        .addChild(test, "test")
        .build();
  }

  private @NotNull CommandResult remove(final CommandContext context) {
    ResourceKey key = context.requireOne(keys.ruleKey);
    String stringKey = key.asString();
    RestrictionRule rule = ruleService.remove(key);
    if (Objects.isNull(rule)) {
      TextComponent.Builder builder = Component.text();
      builder.append(Component.translatable("epicbanitem.command.remove.notExist"));
      //noinspection deprecation
      ruleService
          .keys()
          .map(ResourceKey::asString)
          .min(Comparator.comparingInt(k -> StringUtils.getLevenshteinDistance(k, stringKey)))
          .map(
              it ->
                  Component.translatable("epicbanitem.command.suggestRule")
                      .args(
                          Component.text(it)
                              .clickEvent(ClickEvent.suggestCommand("ebi remove " + it))))
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
            .one(keys.query)
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
    UpdateExpression updateExpression = context.requireOne(keys.update);
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
    player.sendMessage(
        Component.translatable("epicbanitem.command.update.result")
            .color(NamedTextColor.GREEN)
            .append(operation.asComponent()));
    return CommandResult.success();
  }

  private @NotNull CommandResult create(CommandContext context) {
    if (!(context.cause().root() instanceof ServerPlayer))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED));
    final ServerPlayer player = (ServerPlayer) context.cause().root();
    final RestrictionPreset preset = context.one(keys.preset).orElse(RestrictionPresets.TYPE.get());
    final ResourceKey name = context.requireOne(keys.ruleName);
    final Optional<RootQueryExpression> expression = context.one(keys.query);
    DataContainer expressionView = DataContainer.createNew();
    final boolean isBlock = context.hasFlag(flags.block);
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
    ruleService.register(name, new RestrictionRuleImpl(new RootQueryExpression(expressionView)));
    try {
      player.sendMessage(
          Component.translatable("epicbanitem.command.create.success")
              .args(
                  Component.text(name.value())
                      .hoverEvent(Component.text(DataFormats.JSON.get().write(expressionView))))
              .append(Component.space())
              .append(
                  Components.EDIT
                      .color(NamedTextColor.GRAY)
                      .clickEvent(
                          SpongeComponents.executeCallback(
                              cause -> {
                                try {
                                  Sponge.server()
                                      .commandManager()
                                      .process(
                                          player,
                                          MessageFormat.format(
                                              "{0} edit {1}", EpicBanItem.NAMESPACE, name));
                                } catch (CommandException e) {
                                  throw new RuntimeException(e);
                                }
                              }))));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return CommandResult.success();
  }

  private @NotNull CommandResult test(CommandContext context) throws CommandException {
    if (!(context.cause().root() instanceof ServerPlayer))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.needPlayer", NamedTextColor.RED));
    final ServerPlayer player = (ServerPlayer) context.cause().root();
    final ServerWorld world = context.one(keys.world).orElse(player.world());
    final boolean isBlock = context.hasFlag(flags.block);
    ItemStack targetObject =
        targetObject(player, isBlock)
            .orElseThrow(
                () ->
                    new CommandException(
                        isBlock
                            ? Component.translatable("epicbanitem.command.needBlock")
                            : Component.translatable("epicbanitem.command.needItem")));
    DataContainer targetView = targetObject.toContainer();
    final Stream<? extends RestrictionTrigger> triggers =
        context.hasAny(keys.trigger)
            ? context.all(keys.trigger).stream()
            : RestrictionTriggers.registry().stream();
    TranslatableComponent.Builder component =
        Component.translatable("epicbanitem.command.test.result").toBuilder();
    predicateService
        .rules(targetObject.type().key(RegistryTypes.ITEM_TYPE))
        .forEach(
            rule -> {
              player.sendMessage(
                  Component.translatable(
                      "epicbanitem.command.test.result.rule", rule.asComponent()));
              triggers
                  .map(
                      trigger ->
                          restrictionService.restrict(rule, targetView, world, trigger, null))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .forEach(
                      operation -> {
                        if (component.children().isEmpty()) {
                          component
                              .append(Component.space())
                              .append(
                                  Component.translatable(
                                      "epicbanitem.command.test.result.rule.operations"));
                        }
                        component.append(Component.newline()).append(operation.asComponent());
                      });
            });
    player.sendMessage(component);
    return CommandResult.success();
  }
}