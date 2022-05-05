package team.ebi.epicbanitem;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.RestrictionRuleService;
import team.ebi.epicbanitem.api.RestrictionTrigger;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.ExpressionKeys;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunction;
import team.ebi.epicbanitem.api.expression.QueryExpressions;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateExpressions;
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
import team.ebi.epicbanitem.rule.RestrictionRuleServiceImpl;
import team.ebi.epicbanitem.rule.RestrictionRulesStorage;
import team.ebi.epicbanitem.rule.RulePredicateServiceImpl;
import team.ebi.epicbanitem.trigger.SimpleRestrictionTrigger;
import team.ebi.epicbanitem.trigger.UseRestrictionTrigger;

@Singleton
public class EBIRegistries {
  public static DefaultedRegistryType<RestrictionTrigger> TRIGGER;
  public static DefaultedRegistryType<QueryExpressionFunction> QUERY_EXPRESSION;
  public static DefaultedRegistryType<UpdateExpressionFunction> UPDATE_EXPRESSION;

  @Inject private RulePredicateServiceImpl rulePredicateService;

  @Inject
  public EBIRegistries(
      PluginContainer pluginContainer,
      EventManager eventManager,
      RestrictionRulesStorage rulesStorage) {
    eventManager.registerListeners(pluginContainer, this);
    Objects.requireNonNull(rulesStorage);
  }

  @Listener
  public void onRegisterRegistry(RegisterRegistryEvent.EngineScoped<Server> event) {
    TRIGGER =
        event
            .register(
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
                        .build())
            .asDefaultedType(Sponge::server);

    QUERY_EXPRESSION =
        event
            .register(
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
                        .put(
                            EpicBanItem.key(ExpressionKeys.ELEM_MATCH),
                            ElemMatchQueryExpression::new)
                        // Other
                        .put(EpicBanItem.key(ExpressionKeys.EXISTS), ExistsQueryExpression::new)
                        .put(EpicBanItem.key(ExpressionKeys.REGEX), RegexQueryExpression::new)
                        .build())
            .asDefaultedType(Sponge::server);

    UPDATE_EXPRESSION =
        event
            .register(
                EpicBanItem.key("update_expression"),
                false,
                () ->
                    ImmutableMap.<ResourceKey, UpdateExpressionFunction>builder()
                        .put(
                            EpicBanItem.key(ExpressionKeys.SET),
                            view -> new ObjectUpdateExpression(SetUpdateExpression::new, view))
                        .put(
                            EpicBanItem.key(ExpressionKeys.UNSET),
                            view -> new ObjectUpdateExpression(UnsetUpdateExpression::new, view))
                        .put(
                            EpicBanItem.key(ExpressionKeys.RENAME),
                            view -> new ObjectUpdateExpression(RenameUpdateExpression::new, view))
                        .put(
                            EpicBanItem.key(ExpressionKeys.POP),
                            view -> new ObjectUpdateExpression(PopUpdateExpression::new, view))
                        .put(
                            EpicBanItem.key(ExpressionKeys.PULL),
                            view -> new ObjectUpdateExpression(PullUpdateExpression::new, view))
                        .put(
                            EpicBanItem.key(ExpressionKeys.INC),
                            view -> new ObjectUpdateExpression(IncUpdateExpression::new, view))
                        .put(
                            EpicBanItem.key(ExpressionKeys.MUL),
                            view -> new ObjectUpdateExpression(MulUpdateExpression::new, view))
                        .build())
            .asDefaultedType(Sponge::server);
  }

  @Listener
  public void provideRestrictionRuleService(
      ProvideServiceEvent.EngineScoped<RestrictionRuleService> event) {
    if (!(event.engine() instanceof Server)) return;
    event.suggest(RestrictionRuleServiceImpl::new);
  }

  @Listener
  public void provideRulePredicateService(
      ProvideServiceEvent.EngineScoped<RulePredicateService> event) {
    if (!(event.engine() instanceof Server)) return;
    event.suggest(() -> rulePredicateService);
  }

  @Listener(order = Order.POST)
  public void onRegisterRegistryValue(RegisterRegistryValueEvent.EngineScoped<Server> event) {
    QueryExpressions.EXPRESSIONS = toMap(QUERY_EXPRESSION);
    UpdateExpressions.EXPRESSIONS = toMap(UPDATE_EXPRESSION);
  }

  private static <T> ImmutableMap<String, T> toMap(DefaultedRegistryType<T> registry) {
    return registry
        .get()
        .streamEntries()
        .reduce(
            ImmutableMap.<String, T>builder(),
            (builder, entry) -> builder.put(entry.key().value(), entry.value()),
            (builder, other) -> other)
        .build();
  }
}
