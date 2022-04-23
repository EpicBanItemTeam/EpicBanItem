package team.ebi.epicbanitem;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
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
import team.ebi.epicbanitem.api.RestrictionTriggers;
import team.ebi.epicbanitem.api.RulePredicateService;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunction;
import team.ebi.epicbanitem.api.expression.QueryExpressions;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateExpressions;
import team.ebi.epicbanitem.rule.RestrictionRulesStorage;
import team.ebi.epicbanitem.rule.RulePredicateServiceImpl;

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
            .register(EpicBanItem.key("restriction_trigger"), false, () -> RestrictionTriggers.DEFAULT_REGISTRIES)
            .asDefaultedType(Sponge::server);

    QUERY_EXPRESSION =
        event
            .register(
                EpicBanItem.key("query_expression"),
                false,
                () -> QueryExpressions.DEFAULT_REGISTRIES)
            .asDefaultedType(Sponge::server);

    UPDATE_EXPRESSION =
        event
            .register(
                EpicBanItem.key("update_expression"),
                false,
                () -> UpdateExpressions.DEFAULT_REGISTRIES)
            .asDefaultedType(Sponge::server);
  }

  @Listener
  public void provideRestrictionRuleService(
      ProvideServiceEvent.EngineScoped<RestrictionRuleService> event) {
    if (!(event.engine() instanceof Server)) return;
    // TODO

  }

  @Listener
  public void provideRulePredicateService(
      ProvideServiceEvent.EngineScoped<RulePredicateService> event) {
    if (!(event.engine() instanceof Server)) return;
    event.suggest(() -> rulePredicateService);
  }

  @Listener(order = Order.POST)
  public void onRegisterRegistryValue(RegisterRegistryValueEvent.EngineScoped<Server> event) {
    QueryExpressions.EXPRESSIONS = QueryExpressions.toMap();
    UpdateExpressions.EXPRESSIONS = UpdateExpressions.toMap();
  }
}
