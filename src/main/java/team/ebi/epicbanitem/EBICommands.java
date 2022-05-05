package team.ebi.epicbanitem;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import java.util.Optional;
import java.util.function.Predicate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataHolder.Mutable;
import org.spongepowered.api.data.DataTransactionResult;
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
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.expression.QueryExpression;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.util.DataSerializableValueParser;
import team.ebi.epicbanitem.util.DataViewComponentRenderer;

public final class EBICommands {

  private static final Predicate<CommandCause> MUTABLE_DATA_HOLDER =
      cause -> cause.root() instanceof DataHolder.Mutable;

  private static final Predicate<CommandCause> TARGET_BLOCK =
      cause -> cause.targetBlock().isPresent();

  public static final Command.Parameterized QUERY =
      Command.builder()
          .shortDescription(Component.translatable("command.query.description"))
          .extendedDescription(Component.translatable("command.query.description.extended"))
          .executionRequirements(
              MUTABLE_DATA_HOLDER.and(
                  cause -> cause.hasPermission(EpicBanItem.permission("command.query"))))
          .addFlag(
              // TODO 需要测试 flag 是否强制指向方块
              Flag.builder().aliases("block", "b").build())
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
                Mutable holder = (Mutable) src;
                // Argument > Last > Empty
                QueryExpression expression =
                    context
                        .one(Parameter.key("query", QueryExpression.class))
                        .orElse(
                            holder.getOrElse(
                                EBIKeys.LAST_QUERY,
                                new RootQueryExpression(
                                    DataContainer.createNew(), DataQuery.of())));
                DataTransactionResult transaction = holder.offer(EBIKeys.LAST_QUERY, expression);
                Optional<ItemStackSnapshot> heldItem = Optional.empty();
                if (src instanceof Equipable) {
                  Equipable equipable = (Equipable) src;
                  heldItem =
                      equipable
                          .equipped(EquipmentTypes.MAIN_HAND.get())
                          .map(ItemStack::createSnapshot);
                  if (!heldItem.isPresent())
                    heldItem =
                        equipable
                            .equipped(EquipmentTypes.OFF_HAND.get())
                            .map(ItemStack::createSnapshot);
                }
                heldItem = heldItem.filter(it -> !it.isEmpty());
                Optional<? extends BlockEntity> targetBlock = Optional.empty();
                if (src instanceof Living) {
                  Living living = (Living) src;
                  Optional<RayTraceResult<LocatableBlock>> result =
                      RayTrace.block()
                          .select(RayTrace.nonAir())
                          .limit(5)
                          .sourceEyePosition(living)
                          .direction(living)
                          .execute();
                  targetBlock =
                      result
                          .map(RayTraceResult::selectedObject)
                          .map(Locatable::location)
                          .flatMap(Location::blockEntity);
                }
                if (isBlock && !targetBlock.isPresent())
                  throw new CommandException(Component.translatable("command.query.needBlock"));
                if (!isBlock && !heldItem.isPresent())
                  throw new CommandException(Component.translatable("command.query.needItem"));
                SerializableDataHolder serializable = isBlock ? targetBlock.get() : heldItem.get();
                DataView container = serializable.toContainer();
                Optional<QueryResult> result = expression.query(container);
                Audience audience = context.cause().audience();
                Builder pagination = Sponge.serviceProvider().paginationService().builder();
                Component objectName =
                    serializable
                        .get(Keys.DISPLAY_NAME)
                        .orElse(
                            isBlock
                                ? targetBlock.get().block().type().asComponent()
                                : ItemTypes.AIR.get().asComponent());
                if (!isBlock) objectName = objectName.hoverEvent(heldItem.get());
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

  public static final Command.Parameterized ROOT =
      Command.builder()
          .shortDescription(Component.translatable("command.root.description"))
          .permission(EpicBanItem.permission("command.root"))
          .addChild(QUERY, "query", "q")
          .build();

  @Inject
  public EBICommands(PluginContainer plugin, EventManager eventManager) {
    eventManager.registerListener(
        EventListenerRegistration.builder(
                new TypeToken<RegisterCommandEvent<Command.Parameterized>>() {})
            .plugin(plugin)
            .listener(event -> event.register(plugin, ROOT, "epicbanitem", "ebi"))
            .order(Order.DEFAULT)
            .build());
  }
}
