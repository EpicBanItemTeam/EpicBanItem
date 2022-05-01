package team.ebi.epicbanitem;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.function.Predicate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataHolder.Mutable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
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
          .permission(EpicBanItem.permission("command.query"))
          .executionRequirements(MUTABLE_DATA_HOLDER)
          .addFlag(
              // TODO 需要测试 flag 是否强制指向方块
              Flag.builder().aliases("block", "b").setRequirement(TARGET_BLOCK).build())
          .addParameter(
              Parameter.builder(QueryExpression.class)
                  .key("query")
                  .addParser(new DataSerializableValueParser<>(RootQueryExpression.class))
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
                                new RootQueryExpression(DataContainer.createNew())));
                holder.offer(EBIKeys.LAST_QUERY, expression);
                BlockSnapshot targetBlock = context.cause().targetBlock().get();
                ItemStackSnapshot heldItem = ItemStackSnapshot.empty();
                if (src instanceof Equipable) {
                  Equipable equipable = (Equipable) src;
                  heldItem =
                      equipable
                          .equipped(EquipmentTypes.MAIN_HAND.get())
                          .orElse(
                              equipable
                                  .equipped(EquipmentTypes.OFF_HAND.get())
                                  .orElse(ItemStack.empty()))
                          .createSnapshot();
                }
                SerializableDataHolder serializable = isBlock ? targetBlock : heldItem;
                DataContainer container = serializable.toContainer();
                Optional<QueryResult> result = expression.query(container);
                Audience audience = src instanceof Audience ? (Audience) src : Sponge.server();
                Builder pagination = Sponge.serviceProvider().paginationService().builder();
                Component objectName =
                    serializable
                        .get(Keys.DISPLAY_NAME)
                        .orElse(
                            isBlock
                                ? targetBlock.state().type().asComponent()
                                : ItemTypes.AIR.get().asComponent());
                if (!isBlock) objectName = objectName.hoverEvent(heldItem);
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
}
