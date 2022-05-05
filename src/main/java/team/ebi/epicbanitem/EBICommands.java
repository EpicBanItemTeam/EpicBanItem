package team.ebi.epicbanitem;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.data.DataHolder.Mutable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.Living;
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

public final class EBICommands {

  private static final Predicate<CommandCause> MUTABLE_DATA_HOLDER =
      cause -> cause.root() instanceof Mutable;

  private static final Cache<UUID, QueryExpression> USED_EXPRESSION =
      Caffeine.newBuilder().maximumSize(32).build();

  public static final Parameterized QUERY =
      Command.builder()
          .shortDescription(Component.translatable("command.query.description"))
          .extendedDescription(Component.translatable("command.query.description.extended"))
          .executionRequirements(
              MUTABLE_DATA_HOLDER.and(
                  cause -> cause.hasPermission(EpicBanItem.permission("command.query"))))
          .addFlag(Flag.builder().aliases("block", "b").build())
          .addParameter(
              Parameter.builder(QueryExpression.class)
                  .key("query")
                  .addParser(new DataSerializableValueParser<>(RootQueryExpression.class))
                  .optional()
                  .build())
          .executor(
              context -> {
                boolean isBlock = context.hasFlag("block");
                Object src = context.cause().root();
                UUID uuid = ((Identified) src).identity().uuid();
                // Argument > Last > Empty
                QueryExpression expression =
                    context
                        .one(Parameter.key("query", QueryExpression.class))
                        .orElse(
                            Objects.requireNonNull(
                                USED_EXPRESSION.get(
                                    uuid,
                                    it ->
                                        new RootQueryExpression(
                                            DataContainer.createNew(), DataQuery.of()))));
                USED_EXPRESSION.put(uuid, expression);
                SerializableDataHolder targetObject =
                    Optional.of(isBlock)
                        .filter(Boolean::booleanValue)
                        .filter(it -> src instanceof Living)
                        .map(it -> (Living) src)
                        .<SerializableDataHolder>flatMap(EBICommands::targetBlock)
                        // .map(EBICommands::fromBlock)
                        // .filter(Predicate.not(ItemStack::isEmpty))
                        .or(
                            () ->
                                Optional.of(src)
                                    .filter(Predicates.instanceOf(Equipable.class))
                                    .map(it -> (Equipable) it)
                                    .flatMap(EBICommands::heldItem)
                                    .map(ItemStack::createSnapshot)
                                    .filter(Predicate.not(ItemStackSnapshot::isEmpty)))
                        .orElseThrow(
                            () -> {
                              if (!isBlock)
                                return new CommandException(
                                    Component.translatable("command.query.needItem"));
                              else
                                return new CommandException(
                                    Component.translatable("command.query.needBlock"));
                            });
                DataView container = targetObject.toContainer();
                Optional<QueryResult> result = expression.query(container);
                Audience audience = context.cause().audience();
                PaginationList.Builder pagination =
                    Sponge.serviceProvider().paginationService().builder();
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
                        ? Component.translatable("command.query.success")
                        : Component.translatable("command.query.failed");
                ImmutableList<Component> components =
                    DataViewComponentRenderer.render(container, result.orElse(null));
                pagination.title(objectName).header(header).contents(components).sendTo(audience);
                return CommandResult.success();
              })
          .build();

  public static final Parameterized ROOT =
      Command.builder()
          .shortDescription(Component.translatable("command.root.description"))
          .permission(EpicBanItem.permission("command.root"))
          .addChild(QUERY, "query", "q")
          .build();

  @Inject
  public EBICommands(PluginContainer plugin, EventManager eventManager) {
    eventManager.registerListener(
        EventListenerRegistration.builder(new TypeToken<RegisterCommandEvent<Parameterized>>() {})
            .plugin(plugin)
            .listener(event -> event.register(plugin, ROOT, "epicbanitem", "ebi"))
            .order(Order.DEFAULT)
            .build());
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
