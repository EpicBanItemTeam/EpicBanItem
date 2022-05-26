/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem;

import java.util.Objects;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.plugin.PluginContainer;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import team.ebi.epicbanitem.api.ItemQueries;
import team.ebi.epicbanitem.api.RestrictionPreset;
import team.ebi.epicbanitem.api.expression.*;
import team.ebi.epicbanitem.api.trigger.RestrictionTrigger;
import team.ebi.epicbanitem.expression.ArrayableQueryExpression;
import team.ebi.epicbanitem.expression.ObjectUpdateExpression;
import team.ebi.epicbanitem.expression.query.*;
import team.ebi.epicbanitem.expression.update.*;
import team.ebi.epicbanitem.rule.RestrictionRulesStorage;
import team.ebi.epicbanitem.trigger.EquipRestrictionTrigger;
import team.ebi.epicbanitem.trigger.PlaceRestrictionTrigger;
import team.ebi.epicbanitem.trigger.RestrictionTriggerImpl;
import team.ebi.epicbanitem.trigger.UseRestrictionTrigger;
import team.ebi.epicbanitem.util.data.DataUtils;

@Singleton
public final class EBIRegistries {

    public static final DefaultedRegistryType<RestrictionTrigger> TRIGGER = key("restriction_trigger");
    public static final DefaultedRegistryType<QueryExpressionFunction> QUERY_EXPRESSION = key("query_expression");
    public static final DefaultedRegistryType<UpdateExpressionFunction> UPDATE_EXPRESSION = key("update_expression");
    public static final DefaultedRegistryType<RestrictionPreset> PRESET = key("preset");

    @Inject
    private Injector injector;

    @Inject
    EBIRegistries(PluginContainer pluginContainer, EventManager eventManager) {
        eventManager.registerListeners(pluginContainer, this);
    }

    private static <T> ImmutableMap<String, T> asMap(DefaultedRegistryType<T> registry) {
        return registry.get()
                .streamEntries()
                .reduce(
                        ImmutableMap.<String, T>builder(),
                        (builder, entry) -> builder.put("$" + entry.key().value(), entry.value()),
                        (builder, other) -> other)
                .build();
    }

    private static <V> DefaultedRegistryType<V> key(final String key) {
        return RegistryType.of(RegistryRoots.SPONGE, EpicBanItem.key(Objects.requireNonNull(key, "key")))
                .asDefaultedType(Sponge::server);
    }

    @Listener
    public void onRegisterRegistry(RegisterRegistryEvent.EngineScoped<Server> event) {
        event.register(
                EpicBanItem.key("restriction_trigger"),
                false,
                () -> ImmutableMap.<ResourceKey, RestrictionTrigger>builder()
                        .put(EpicBanItem.key("place"), injector.getInstance(PlaceRestrictionTrigger.class))
                        .put(EpicBanItem.key("break"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("pickup"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("throw"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("drop"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("use"), injector.getInstance(UseRestrictionTrigger.class))
                        .put(EpicBanItem.key("interact"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("be_interacted"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("join"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("equip"), injector.getInstance(EquipRestrictionTrigger.class))
                        .put(EpicBanItem.key("be_equipped"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("craft"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("store"), new RestrictionTriggerImpl())
                        .put(EpicBanItem.key("click"), new RestrictionTriggerImpl())
                        .build());

        event.register(
                EpicBanItem.key("query_expression"),
                false,
                () -> ImmutableMap.<ResourceKey, QueryExpressionFunction>builder()
                        // Logical
                        .put(EpicBanItem.key(ExpressionKeys.OR), OrQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.NOR), NorQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.AND), AndQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.NOT), NotQueryExpression::new)
                        // Compare
                        .put(
                                EpicBanItem.key(ExpressionKeys.EQ),
                                (view, query) -> new ArrayableQueryExpression(new EqQueryExpression(view, query)))
                        .put(
                                EpicBanItem.key(ExpressionKeys.NE),
                                (view, query) -> new ArrayableQueryExpression(new NeQueryExpression(view, query)))
                        .put(
                                EpicBanItem.key(ExpressionKeys.GT),
                                (view, query) -> new ArrayableQueryExpression(new GtQueryExpression(view, query)))
                        .put(
                                EpicBanItem.key(ExpressionKeys.LT),
                                (view, query) -> new ArrayableQueryExpression(new LtQueryExpression(view, query)))
                        .put(
                                EpicBanItem.key(ExpressionKeys.GTE),
                                (view, query) -> new ArrayableQueryExpression(new GteQueryExpression(view, query)))
                        .put(
                                EpicBanItem.key(ExpressionKeys.LTE),
                                (view, query) -> new ArrayableQueryExpression(new LteQueryExpression(view, query)))
                        // In
                        .put(EpicBanItem.key(ExpressionKeys.IN), InQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.NIN), NinQueryExpression::new)
                        // Array
                        .put(EpicBanItem.key(ExpressionKeys.SIZE), SizeQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.ALL), AllQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.ELEM_MATCH), ElemMatchQueryExpression::new)
                        // Other
                        .put(EpicBanItem.key(ExpressionKeys.EXISTS), ExistsQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.REGEX), RegexQueryExpression::new)
                        .build());

        event.register(
                EpicBanItem.key("update_expression"),
                false,
                () -> ImmutableMap.<ResourceKey, UpdateExpressionFunction>builder()
                        .put(
                                EpicBanItem.key(ExpressionKeys.SET),
                                (view, query) -> new ObjectUpdateExpression(SetUpdateExpression::new, view, query))
                        .put(
                                EpicBanItem.key(ExpressionKeys.UNSET),
                                (view, query) -> new ObjectUpdateExpression(
                                        (ignored, currentQuery) -> new UnsetUpdateExpression(currentQuery),
                                        view,
                                        query))
                        .put(
                                EpicBanItem.key(ExpressionKeys.RENAME),
                                (view, query) -> new ObjectUpdateExpression(RenameUpdateExpression::new, view, query))
                        .put(
                                EpicBanItem.key(ExpressionKeys.POP),
                                (view, query) -> new ObjectUpdateExpression(PopUpdateExpression::new, view, query))
                        .put(
                                EpicBanItem.key(ExpressionKeys.PULL),
                                (view, query) -> new ObjectUpdateExpression(PullUpdateExpression::new, view, query))
                        .put(
                                EpicBanItem.key(ExpressionKeys.INC),
                                (view, query) -> new ObjectUpdateExpression(IncUpdateExpression::new, view, query))
                        .put(
                                EpicBanItem.key(ExpressionKeys.MUL),
                                (view, query) -> new ObjectUpdateExpression(MulUpdateExpression::new, view, query))
                        .build());

        event.register(EpicBanItem.key("preset"), false, () -> ImmutableMap.<ResourceKey, RestrictionPreset>builder()
                .put(EpicBanItem.key("type"), view -> DataContainer.createNew()
                        .set(
                                ItemQueries.ITEM_TYPE,
                                view.get(ItemQueries.ITEM_TYPE).orElseThrow()))
                .put(EpicBanItem.key("all"), DataUtils::dataToExpression)
                .put(EpicBanItem.key("empty"), view -> DataContainer.createNew())
                .build());
    }

    @Listener(order = Order.POST)
    public void onRegisterRegistryValue(RegisterRegistryValueEvent.EngineScoped<Server> event) {
        QueryExpressionFunctions.expressions = asMap(QUERY_EXPRESSION);
        UpdateExpressionFunctions.expressions = asMap(UPDATE_EXPRESSION);
    }

    @Listener
    public void onLoadedGame(LoadedGameEvent event) {
        Objects.requireNonNull(injector.getInstance(RestrictionRulesStorage.class));
    }
}
