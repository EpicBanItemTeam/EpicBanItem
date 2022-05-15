package team.ebi.epicbanitem;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.ItemQueries;
import team.ebi.epicbanitem.api.RestrictionPreset;
import team.ebi.epicbanitem.api.RestrictionTrigger;
import team.ebi.epicbanitem.api.expression.ExpressionKeys;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunction;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunctions;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunctions;
import team.ebi.epicbanitem.expression.ArrayableQueryExpression;
import team.ebi.epicbanitem.expression.ObjectUpdateExpression;
import team.ebi.epicbanitem.expression.query.AllQueryExpression;
import team.ebi.epicbanitem.expression.query.AndQueryExpression;
import team.ebi.epicbanitem.expression.query.ElemMatchQueryExpression;
import team.ebi.epicbanitem.expression.query.EqQueryExpression;
import team.ebi.epicbanitem.expression.query.ExistsQueryExpression;
import team.ebi.epicbanitem.expression.query.GtQueryExpression;
import team.ebi.epicbanitem.expression.query.GteQueryExpression;
import team.ebi.epicbanitem.expression.query.InQueryExpression;
import team.ebi.epicbanitem.expression.query.LtQueryExpression;
import team.ebi.epicbanitem.expression.query.LteQueryExpression;
import team.ebi.epicbanitem.expression.query.NeQueryExpression;
import team.ebi.epicbanitem.expression.query.NinQueryExpression;
import team.ebi.epicbanitem.expression.query.NorQueryExpression;
import team.ebi.epicbanitem.expression.query.NotQueryExpression;
import team.ebi.epicbanitem.expression.query.OrQueryExpression;
import team.ebi.epicbanitem.expression.query.RegexQueryExpression;
import team.ebi.epicbanitem.expression.query.SizeQueryExpression;
import team.ebi.epicbanitem.expression.update.IncUpdateExpression;
import team.ebi.epicbanitem.expression.update.MulUpdateExpression;
import team.ebi.epicbanitem.expression.update.PopUpdateExpression;
import team.ebi.epicbanitem.expression.update.PullUpdateExpression;
import team.ebi.epicbanitem.expression.update.RenameUpdateExpression;
import team.ebi.epicbanitem.expression.update.SetUpdateExpression;
import team.ebi.epicbanitem.expression.update.UnsetUpdateExpression;
import team.ebi.epicbanitem.rule.RestrictionRulesStorage;
import team.ebi.epicbanitem.trigger.SimpleRestrictionTrigger;
import team.ebi.epicbanitem.trigger.UseRestrictionTrigger;

@Singleton
public final class EBIRegistries {

  public static final DefaultedRegistryType<RestrictionTrigger> TRIGGER = key(
      "restriction_trigger");
  public static final DefaultedRegistryType<QueryExpressionFunction> QUERY_EXPRESSION = key(
      "query_expression");
  public static final DefaultedRegistryType<UpdateExpressionFunction> UPDATE_EXPRESSION = key(
      "update_expression");
  public static final DefaultedRegistryType<RestrictionPreset> PRESET = key("preset");

  @Inject
  EBIRegistries(
      PluginContainer pluginContainer,
      EventManager eventManager,
      RestrictionRulesStorage rulesStorage) {
    eventManager.registerListeners(pluginContainer, this);
    Objects.requireNonNull(rulesStorage);
  }

  private static <T> ImmutableMap<String, T> asMap(DefaultedRegistryType<T> registry) {
    return registry
        .get()
        .streamEntries()
        .reduce(
            ImmutableMap.<String, T>builder(),
            (builder, entry) -> builder.put("$" + entry.key().value(), entry.value()),
            (builder, other) -> other)
        .build();
  }

  private static <V> DefaultedRegistryType<V> key(final String key) {
    return RegistryType.of(
            RegistryRoots.SPONGE, EpicBanItem.key(Objects.requireNonNull(key, "key")))
        .asDefaultedType(Sponge::server);
  }

  @Listener
  public void onRegisterRegistry(RegisterRegistryEvent.EngineScoped<Server> event) {
    event.register(
        EpicBanItem.key("restriction_trigger"),
        false,
        () ->
            ImmutableMap.<ResourceKey, RestrictionTrigger>builder()
                .put(EpicBanItem.key("use"), new UseRestrictionTrigger())
                .put(EpicBanItem.key("equip"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("craft"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("pickup"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("click"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("throw"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("drop"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("place"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("break"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("interact"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("join"), new SimpleRestrictionTrigger())
                .put(EpicBanItem.key("store"), new SimpleRestrictionTrigger())
                .build());

    event.register(
        EpicBanItem.key("query_expression"),
        false,
        () ->
            ImmutableMap.<ResourceKey, QueryExpressionFunction>builder()
                // Logical
                .put(EpicBanItem.key(ExpressionKeys.OR), OrQueryExpression::new)
                .put(EpicBanItem.key(ExpressionKeys.NOR), NorQueryExpression::new)
                .put(EpicBanItem.key(ExpressionKeys.AND), AndQueryExpression::new)
                .put(EpicBanItem.key(ExpressionKeys.NOT), NotQueryExpression::new)
                // Compare
                .put(
                    EpicBanItem.key(ExpressionKeys.EQ),
                    (view, query) ->
                        new ArrayableQueryExpression(new EqQueryExpression(view, query)))
                .put(
                    EpicBanItem.key(ExpressionKeys.NE),
                    (view, query) ->
                        new ArrayableQueryExpression(new NeQueryExpression(view, query)))
                .put(
                    EpicBanItem.key(ExpressionKeys.GT),
                    (view, query) ->
                        new ArrayableQueryExpression(new GtQueryExpression(view, query)))
                .put(
                    EpicBanItem.key(ExpressionKeys.LT),
                    (view, query) ->
                        new ArrayableQueryExpression(new LtQueryExpression(view, query)))
                .put(
                    EpicBanItem.key(ExpressionKeys.GTE),
                    (view, query) ->
                        new ArrayableQueryExpression(new GteQueryExpression(view, query)))
                .put(
                    EpicBanItem.key(ExpressionKeys.LTE),
                    (view, query) ->
                        new ArrayableQueryExpression(new LteQueryExpression(view, query)))
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
        () ->
            ImmutableMap.<ResourceKey, UpdateExpressionFunction>builder()
                .put(
                    EpicBanItem.key(ExpressionKeys.SET),
                    (view, query) ->
                        new ObjectUpdateExpression(SetUpdateExpression::new, view, query))
                .put(
                    EpicBanItem.key(ExpressionKeys.UNSET),
                    (view, query) ->
                        new ObjectUpdateExpression(
                            (ignored, currentQuery) -> new UnsetUpdateExpression(currentQuery),
                            view,
                            query))
                .put(
                    EpicBanItem.key(ExpressionKeys.RENAME),
                    (view, query) ->
                        new ObjectUpdateExpression(RenameUpdateExpression::new, view, query))
                .put(
                    EpicBanItem.key(ExpressionKeys.POP),
                    (view, query) ->
                        new ObjectUpdateExpression(PopUpdateExpression::new, view, query))
                .put(
                    EpicBanItem.key(ExpressionKeys.PULL),
                    (view, query) ->
                        new ObjectUpdateExpression(PullUpdateExpression::new, view, query))
                .put(
                    EpicBanItem.key(ExpressionKeys.INC),
                    (view, query) ->
                        new ObjectUpdateExpression(IncUpdateExpression::new, view, query))
                .put(
                    EpicBanItem.key(ExpressionKeys.MUL),
                    (view, query) ->
                        new ObjectUpdateExpression(MulUpdateExpression::new, view, query))
                .build());

    event.register(
        EpicBanItem.key("preset"),
        false,
        () ->
            ImmutableMap.<ResourceKey, RestrictionPreset>builder()
                .put(
                    EpicBanItem.key("type"),
                    view ->
                        DataContainer.createNew()
                            .set(
                                ItemQueries.ITEM_TYPE,
                                view.get(ItemQueries.ITEM_TYPE).orElseThrow()))
                .put(
                    EpicBanItem.key("all"),
                    view -> {
                      DataContainer container = DataContainer.createNew();
                      view.values(false).forEach(container::set);
                      return container;
                    })
                .put(EpicBanItem.key("empty"), view -> DataContainer.createNew())
                .build());
  }

  @Listener(order = Order.POST)
  public void onRegisterRegistryValue(RegisterRegistryValueEvent.EngineScoped<Server> event) {
    QueryExpressionFunctions.expressions = asMap(QUERY_EXPRESSION);
    UpdateExpressionFunctions.expressions = asMap(UPDATE_EXPRESSION);
  }
}
