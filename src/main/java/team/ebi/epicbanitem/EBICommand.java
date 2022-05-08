package team.ebi.epicbanitem;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
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
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.util.DataSerializableValueParser;
import team.ebi.epicbanitem.util.DataViewComponentRenderer;
import team.ebi.epicbanitem.util.DataViewUtils;

public final class EBICommand {
  private static final Cache<UUID, QueryExpression> USED_EXPRESSION =
      Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

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
            .extendedDescription(Component.translatable("epicbanitem.command.query.description.extended"))
            .permission(EpicBanItem.permission("command.query"))
            .addFlag(Flag.builder().aliases("block", "b").build())
            .addParameter(
                Parameter.builder(QueryExpression.class)
                    .key("query")
                    .addParser(new DataSerializableValueParser<>(RootQueryExpression.class))
                    .optional()
                    .build())
            .executor(this::query)
            .build();

    return Command.builder()
        .shortDescription(Component.translatable("command.root.description"))
        .permission(EpicBanItem.permission("command.root"))
        .addChild(query, "query", "q")
        .build();
  }

  private @NotNull CommandResult query(final CommandContext context) throws CommandException {
    if (!(context.cause().root() instanceof Player))
      return CommandResult.error(
          Component.translatable("epicbanitem.command.error.needPlayer", NamedTextColor.RED));
    final Player player = (Player) context.cause().root();
    boolean isBlock = context.hasFlag("block");
    UUID uuid = player.identity().uuid();
    // Argument > Last > Empty
    QueryExpression expression =
        context
            .one(Parameter.key("query", QueryExpression.class))
            .orElse(
                Objects.requireNonNull(
                    USED_EXPRESSION.get(
                        uuid,
                        it -> new RootQueryExpression(DataContainer.createNew(), DataQuery.of()))));
    USED_EXPRESSION.put(uuid, expression);
    SerializableDataHolder targetObject =
        Optional.of(isBlock)
            .filter(Boolean::booleanValue)
            .<SerializableDataHolder>flatMap(it -> targetBlock(player))
            // .map(EBICommands::fromBlock)
            // .filter(Predicate.not(ItemStack::isEmpty))
            .or(
                () ->
                    heldItem(player)
                        .map(ItemStack::createSnapshot)
                        .filter(Predicate.not(ItemStackSnapshot::isEmpty)))
            .orElseThrow(
                () -> {
                  if (!isBlock)
                    return new CommandException(Component.translatable("epicbanitem.command.query.needItem"));
                  else
                    return new CommandException(Component.translatable("epicbanitem.command.query.needBlock"));
                });
    DataView container = DataViewUtils.cleanup(targetObject.toContainer());
    Optional<QueryResult> result = expression.query(container);
    Audience audience = context.cause().audience();
    PaginationList.Builder pagination = Sponge.serviceProvider().paginationService().builder();
    Component objectName =
        targetObject
            .get(Keys.DISPLAY_NAME)
            .orElseGet(
                () -> {
                  if (targetObject instanceof BlockSnapshot)
                    return ((BlockSnapshot) targetObject).state().type().asComponent();
                  else return ItemTypes.AIR.get().asComponent();
                });
    if (targetObject instanceof ItemStackSnapshot)
      objectName = objectName.hoverEvent((ItemStackSnapshot) targetObject);
    Component header =
        result.isPresent()
            ? Component.translatable("epicbanitem.command.query.success")
            : Component.translatable("epicbanitem.command.query.failed");
    ImmutableList<Component> components =
        DataViewComponentRenderer.render(container, result.orElse(null));
    pagination.title(objectName).header(header).contents(components).sendTo(audience);
    return CommandResult.success();
  }

  private static Optional<BlockSnapshot> targetBlock(Living living) {
    return RayTrace.block()
        .select(RayTrace.nonAir())
        .limit(3)
        .sourceEyePosition(living)
        .direction(living)
        .execute()
        .map(it -> it.selectedObject().serverLocation().createSnapshot());
  }

  private static ItemStack fromBlock(BlockSnapshot blockSnapshot) {
    return ItemStack.builder().fromBlockSnapshot(blockSnapshot).build();
  }

  private static Optional<ItemStack> heldItem(Equipable equipable) {
    return equipable
        .equipped(EquipmentTypes.MAIN_HAND.get())
        .or(() -> equipable.equipped(EquipmentTypes.OFF_HAND.get()));
  }
}
