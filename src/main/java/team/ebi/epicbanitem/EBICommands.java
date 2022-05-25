/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import org.spongepowered.api.command.parameter.Parameter;
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
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.RestrictionPresets;
import team.ebi.epicbanitem.api.RestrictionService;
import team.ebi.epicbanitem.api.RestrictionTrigger;
import team.ebi.epicbanitem.api.RestrictionTriggers;
import team.ebi.epicbanitem.api.expression.ExpressionQueries;
import team.ebi.epicbanitem.api.expression.ExpressionService;
import team.ebi.epicbanitem.api.expression.QueryResult;
import team.ebi.epicbanitem.api.rule.RestrictionRuleService;
import team.ebi.epicbanitem.api.rule.RulePredicateService;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;
import team.ebi.epicbanitem.util.Components;
import team.ebi.epicbanitem.util.RestrictionRuleRenderer;
import team.ebi.epicbanitem.util.command.Flags;
import team.ebi.epicbanitem.util.command.Parameters;
import team.ebi.epicbanitem.util.data.DataViewRenderer;

import static team.ebi.epicbanitem.util.Components.*;
import static team.ebi.epicbanitem.util.EntityUtils.*;
import static team.ebi.epicbanitem.util.data.DataUtils.objectName;

@Singleton
public final class EBICommands {

    private final Cache<UUID, RootQueryExpression> usedQuery =
            Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();

    @Inject
    private ExpressionService expressionService;

    @Inject
    private RestrictionRuleService ruleService;

    @Inject
    private RestrictionService restrictionService;

    @Inject
    private RulePredicateService predicateService;

    @Inject
    private Parameters parameters;

    @Inject
    private Parameters.Keys keys;

    @Inject
    private Flags flags;

    public Command.@NotNull Parameterized buildCommand() {
        final var query = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.query.description"))
                .extendedDescription(Component.translatable("epicbanitem.command.query.description.extended"))
                .permission(EpicBanItem.permission("command.query"))
                .addFlag(flags.block)
                .addParameter(parameters.query.optional().key(keys.query).build())
                .terminal(true)
                .executor(this::query)
                .build();

        final var update = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.update.description"))
                .extendedDescription(Component.translatable("epicbanitem.command.update.description.extended"))
                .permission(EpicBanItem.permission("command.update"))
                .addFlag(flags.block)
                .addParameter(parameters.update.key(keys.update).build())
                .executor(this::update)
                .build();

        final var test = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.test.description"))
                .extendedDescription(Component.translatable("epicbanitem.command.test.description.extended"))
                .permission(EpicBanItem.permission("command.test"))
                .addFlags(flags.block, flags.trigger, flags.world)
                .terminal(true)
                .executor(this::test)
                .build();

        final var create = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.create.description"))
                .extendedDescription(Component.translatable("epicbanitem.command.create.description.extended"))
                .permission(EpicBanItem.permission("command.create"))
                .addFlags(flags.preset, flags.block)
                .addParameters(
                        parameters.ruleName.key(keys.ruleName).build(),
                        parameters.query.key(keys.query).optional().build())
                .executor(this::create)
                .build();

        final var info = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.info.description"))
                .permission(EpicBanItem.permission("command.info"))
                .addParameters(parameters.rule.key(keys.rule).build())
                .executor(this::info)
                .build();

        final var remove = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.remove.description"))
                .permission(EpicBanItem.permission("command.remove"))
                .addParameter(parameters.ruleKey.key(keys.ruleKey).build())
                .executor(this::remove)
                .build();

        final var list = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.list.description"))
                .permission(EpicBanItem.permission("command.list"))
                .addParameters(
                        parameters.predicate.key(keys.predicate).optional().build())
                .executor(this::list)
                .build();

        return Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.root.description"))
                .permission(EpicBanItem.permission("command.root"))
                .addChild(query, "query")
                .addChild(update, "update")
                .addChild(test, "test")
                .addChild(remove, "remove", "rm")
                .addChild(create, "create")
                .addChild(list, "list", "ls")
                .addChild(info, "info")
                .addChild(buildSetCommand(), "set")
                .build();
    }

    public Command.@NotNull Parameterized buildSetCommand() {
        final var priority = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.priority"))
                .addParameters(Parameter.rangedInteger(1, 10).key("value").build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(Parameter.key("value", Integer.class));
                    ResourceKey key = rule.key();
                    ruleService.register(key, rule.priority(value));
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + key);
                    return CommandResult.success();
                })
                .build();

        final var world = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.world"))
                .addParameters(
                        Parameter.world().key("world").build(),
                        Parameter.enumValue(Tristate.class).key("value").build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var serverWorld = context.requireOne(Parameter.key("world", ServerWorld.class));
                    final var value = context.requireOne(Parameter.key("value", Tristate.class));
                    rule.worldStates().put(serverWorld.key(), value);
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + rule.key());
                    return CommandResult.success();
                })
                .build();

        final var worldDefault = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.worldDefault"))
                .addParameters(Parameter.bool().key("value").build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(Parameter.key("value", Boolean.class));
                    rule.worldStates().update(value);
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + rule.key());
                    return CommandResult.success();
                })
                .build();

        final var trigger = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.trigger"))
                .addParameters(
                        Parameter.registryElement(
                                        TypeToken.get(RestrictionTrigger.class),
                                        EBIRegistries.TRIGGER,
                                        EpicBanItem.NAMESPACE)
                                .key("trigger")
                                .build(),
                        Parameter.enumValue(Tristate.class).key("value").build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var restrictionTrigger =
                            context.requireOne(Parameter.key("trigger", RestrictionTrigger.class));
                    final var value = context.requireOne(Parameter.key("value", Tristate.class));
                    rule.triggerStates().put(restrictionTrigger.key(), value);
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + rule.key());
                    return CommandResult.success();
                })
                .build();

        final var triggerDefault = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.triggerDefault"))
                .addParameters(Parameter.bool().key("value").build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(Parameter.key("value", Boolean.class));
                    rule.triggerStates().update(value);
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + rule.key());
                    return CommandResult.success();
                })
                .build();

        final var predicate = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.predicate"))
                .addParameters(parameters.predicate.key(keys.predicate).build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(keys.predicate);
                    ResourceKey key = rule.key();
                    ruleService.register(key, rule.predicate(value));
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + key);
                    return CommandResult.success();
                })
                .build();

        final var cancel = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.cancel"))
                .addParameters(Parameter.bool().key("value").build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(Parameter.key("value", Boolean.class));
                    ResourceKey key = rule.key();
                    ruleService.register(key, rule.needCancel(value));
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + key);
                    return CommandResult.success();
                })
                .build();

        final var query = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.query"))
                .addParameters(parameters.query.key(keys.query).build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(keys.query);
                    ResourceKey key = rule.key();
                    ruleService.register(key, rule.queryExpression(value));
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + key);
                    return CommandResult.success();
                })
                .build();

        final var update = Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description.update"))
                .addParameters(parameters.update.key(keys.update).build())
                .executor(context -> {
                    final var rule = context.requireOne(keys.rule);
                    final var value = context.requireOne(keys.update);
                    ResourceKey key = rule.key();
                    ruleService.register(key, rule.updateExpression(value));
                    ruleService.save();
                    Sponge.server()
                            .commandManager()
                            .process(
                                    context.subject(),
                                    context.cause().audience(),
                                    EpicBanItem.NAMESPACE + " info " + key);
                    return CommandResult.success();
                })
                .build();

        return Command.builder()
                .shortDescription(Component.translatable("epicbanitem.command.set.description"))
                .permission(EpicBanItem.permission("command.set"))
                .addParameters(
                        parameters.rule.key(keys.rule).build(),
                        Parameter.firstOf(
                                Parameter.subcommand(priority, "priority"),
                                Parameter.subcommand(world, "world"),
                                Parameter.subcommand(worldDefault, "worldDefault"),
                                Parameter.subcommand(trigger, "trigger"),
                                Parameter.subcommand(triggerDefault, "triggerDefault"),
                                Parameter.subcommand(predicate, "predicate"),
                                Parameter.subcommand(cancel, "cancel"),
                                Parameter.subcommand(query, "query"),
                                Parameter.subcommand(update, "update")))
                .executor(context -> {
                    throw new UnsupportedOperationException();
                })
                .build();
    }

    private @NotNull CommandResult remove(final @NotNull CommandContext context) {
        var key = context.requireOne(keys.ruleKey);
        var stringKey = key.asString();
        var rule = ruleService.remove(key);
        if (Objects.isNull(rule)) {
            TextComponent.Builder builder = Component.text();
            builder.append(Component.translatable("epicbanitem.command.remove.notExist"));
            //noinspection deprecation
            ruleService
                    .keys()
                    .map(ResourceKey::asString)
                    .min(Comparator.comparingInt(k -> StringUtils.getLevenshteinDistance(k, stringKey)))
                    .map(it -> Component.translatable("epicbanitem.command.suggestRule")
                            .args(Component.text(it))
                            .clickEvent(ClickEvent.suggestCommand("ebi remove " + it)))
                    .ifPresent(it -> builder.append(Component.newline()).append(it));
            return CommandResult.error(builder.build());
        }
        return CommandResult.success();
    }

    private @NotNull CommandResult query(final @NotNull CommandContext context) throws CommandException {
        if (!(context.cause().root() instanceof final Player player)) {
            return CommandResult.error(NEED_PLAYER);
        }
        var isBlock = context.hasFlag("block");
        var uuid = player.identity().uuid();
        // Argument > Last > Empty
        var expression = context.one(keys.query)
                .orElse(Objects.requireNonNull(usedQuery.get(uuid, it -> new RootQueryExpression())));
        usedQuery.put(uuid, expression);
        var targetObject =
                targetObject(player, isBlock).orElseThrow(() -> new CommandException(isBlock ? NEED_BLOCK : NEED_ITEM));
        var container = ExpressionService.cleanup(targetObject.toContainer());
        var result = expression.query(container);
        Sponge.serviceProvider()
                .paginationService()
                .builder()
                .title(objectName(targetObject))
                .header(result.isPresent() ? Component.translatable("epicbanitem.command.query.success") : null)
                .contents(expressionService.renderQueryResult(container, result.orElse(null)))
                .sendTo(context.cause().audience());
        return CommandResult.success();
    }

    private @NotNull CommandResult update(final @NotNull CommandContext context) throws CommandException {
        if (!(context.cause().root() instanceof final Player player)) {
            return CommandResult.error(NEED_PLAYER);
        }
        boolean isBlock = context.hasFlag("block");
        UUID uuid = player.identity().uuid();
        // Last > Empty
        var queryExpression = Objects.requireNonNull(usedQuery.get(uuid, it -> new RootQueryExpression()));
        var updateExpression = context.requireOne(keys.update);
        ItemStack targetObject;
        EquipmentType hand = null;
        BlockSnapshot block = null;
        if (isBlock) {
            block = targetBlock(player).orElseThrow(() -> new CommandException(NEED_BLOCK));
            targetObject = ItemStack.builder().fromBlockSnapshot(block).build();
        } else {
            hand = heldHand(player).orElseThrow(() -> new CommandException(NEED_ITEM));
            targetObject = equipped(player, hand).orElseThrow(() -> new CommandException(NEED_ITEM));
        }
        var container = targetObject.toContainer();
        var cleaned = ExpressionService.cleanup(container);
        var result = queryExpression.query(cleaned).orElse(QueryResult.success());
        var operation = updateExpression.update(result, cleaned);
        var processed = operation.process(cleaned);
        processed.values(false).forEach((query, o) -> {
            container.remove(query);
            container.set(query, o);
        });
        var dataManager = Sponge.dataManager();
        var deserialized = dataManager.deserialize(ItemStack.class, container).orElseThrow();
        if (deserialized.quantity() > deserialized.maxStackQuantity()) {
            deserialized.setQuantity(deserialized.maxStackQuantity());
        }
        if (isBlock) {
            BlockType blockType = deserialized.type().block().orElseThrow();
            BlockState oldState = block.state();
            BlockState newState = BlockState.builder()
                    .blockType(blockType)
                    .addFrom(blockType.defaultState())
                    .addFrom(oldState)
                    .build();
            BlockSnapshot newSnapshot =
                    BlockSnapshot.builder().from(block).blockState(newState).build();
            newSnapshot.restore(true, BlockChangeFlags.DEFAULT_PLACEMENT);
        } else {
            player.equip(hand, deserialized);
        }
        player.sendMessage(Component.translatable("epicbanitem.command.update.result")
                .color(NamedTextColor.GREEN)
                .append(Component.newline())
                .append(operation.asComponent()));
        return CommandResult.success();
    }

    private @NotNull CommandResult create(@NotNull CommandContext context) {
        if (!(context.cause().root() instanceof final ServerPlayer player)) {
            return CommandResult.error(NEED_PLAYER);
        }
        final var preset = context.one(keys.preset).orElse(RestrictionPresets.TYPE.get());
        final var name = context.requireOne(keys.ruleName);
        final var expression = context.one(keys.query);
        var expressionView = DataContainer.createNew();
        final var isBlock = context.hasFlag(flags.block);
        expression.ifPresent(it -> it.expression().toContainer().values(false).forEach(expressionView::set));
        if (isBlock) {
            Optional<BlockSnapshot> block = targetBlock(player);
            Set<DataView> views = block
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
                    .collect(Collectors.toUnmodifiableSet());
            if (!views.isEmpty()) {
                if (views.size() == 1) {
                    views.iterator().next().values(false).forEach(expressionView::set);
                } else {
                    expressionView.set(ExpressionQueries.OR, views);
                }
            } else {
                block.map(it -> ItemStack.builder().fromBlockSnapshot(it).build())
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
        RootQueryExpression finalExpression = new RootQueryExpression(expressionView);
        ruleService.register(name, new RestrictionRuleImpl(finalExpression));
        player.sendMessage(Component.translatable("epicbanitem.command.create.success")
                .args(Component.text(name.value())
                        .hoverEvent(Component.join(
                                JoinConfiguration.newlines(),
                                DataViewRenderer.render(
                                                finalExpression.expression().toContainer())
                                        .stream()
                                        .limit(25)
                                        .toList())))
                .append(Component.space())
                .append(Components.EDIT
                        .color(NamedTextColor.GRAY)
                        .clickEvent(SpongeComponents.executeCallback(cause -> {
                            try {
                                Sponge.server()
                                        .commandManager()
                                        .process(
                                                player,
                                                MessageFormat.format("{0} edit {1}", EpicBanItem.NAMESPACE, name));
                            } catch (CommandException e) {
                                throw new IllegalStateException(e);
                            }
                        }))));
        ruleService.save();
        return CommandResult.success();
    }

    private @NotNull CommandResult test(@NotNull CommandContext context) throws CommandException {
        if (!(context.cause().root() instanceof final ServerPlayer player)) {
            return CommandResult.error(NEED_PLAYER);
        }
        final var world = context.one(keys.world).orElse(player.world());
        final var isBlock = context.hasFlag(flags.block);
        var targetObject =
                targetObject(player, isBlock).orElseThrow(() -> new CommandException(isBlock ? NEED_BLOCK : NEED_ITEM));
        var targetView = ExpressionService.cleanup(targetObject.toContainer());
        final var hasTrigger = context.hasAny(keys.trigger);
        final var triggerArgs = context.all(keys.trigger);
        final var allTriggers = RestrictionTriggers.registry();
        var component = Component.translatable("epicbanitem.command.test.result").toBuilder();
        predicateService
                .rulesWithPriority(targetObject.type().key(RegistryTypes.ITEM_TYPE))
                .forEach(rule -> {
                    player.sendMessage(Component.translatable(
                            "epicbanitem.command.test.result.rule",
                            rule.asComponent()
                                    .hoverEvent(Component.join(
                                            JoinConfiguration.newlines(),
                                            DataViewRenderer.render(rule.queryExpression()
                                                            .toContainer())
                                                    .stream()
                                                    .limit(25)
                                                    .toList()))));
                    (hasTrigger ? triggerArgs.stream() : allTriggers.stream())
                            .map(trigger -> restrictionService.restrict(rule, targetView, world, trigger, null))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(operation -> {
                                if (component.children().isEmpty()) {
                                    component
                                            .append(Component.space())
                                            .append(Component.translatable(
                                                    "epicbanitem.command.test.result.rule.operations"));
                                }
                                component.append(Component.newline()).append(operation.asComponent());
                            });
                });
        player.sendMessage(component);
        return CommandResult.success();
    }

    private @NotNull CommandResult list(@NotNull CommandContext context) {
        final var subject = context.cause().subject();
        final var predicate = context.one(keys.predicate).orElse(RulePredicateService.WILDCARD);
        final var components = predicateService.rule(predicate).stream()
                .map(rule -> {
                    var editComponent = Components.EDIT.color(NamedTextColor.GRAY);
                    if (subject instanceof ServerPlayer player) {
                        editComponent = editComponent.clickEvent(SpongeComponents.executeCallback(cause -> {
                            try {
                                Sponge.server()
                                        .commandManager()
                                        .process(
                                                player,
                                                MessageFormat.format(
                                                        "{0} edit {1}", EpicBanItem.NAMESPACE, rule.key()));
                            } catch (CommandException e) {
                                throw new IllegalStateException(e);
                            }
                        }));
                    }
                    // TODO copy rule
                    try {
                        return rule.asComponent()
                                .hoverEvent(Component.join(
                                        JoinConfiguration.newlines(),
                                        DataViewRenderer.render(
                                                        rule.queryExpression().toContainer())
                                                .stream()
                                                .limit(25)
                                                .toList()))
                                .clickEvent(ClickEvent.suggestCommand(DataFormats.HOCON
                                        .get()
                                        .write(rule.queryExpression().toContainer())))
                                .append(Component.space())
                                .append(editComponent);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        Sponge.serviceProvider()
                .paginationService()
                .builder()
                .title(Component.translatable("epicbanitem.command.list.title"))
                .contents(components)
                .sendTo(context.cause().audience());
        return CommandResult.success();
    }

    private @NotNull CommandResult info(final @NotNull CommandContext context) {
        final var audience = context.cause().audience();
        final var rule = context.requireOne(keys.rule);
        audience.sendMessage(RestrictionRuleRenderer.renderRule(rule));
        return CommandResult.success();
    }
}
