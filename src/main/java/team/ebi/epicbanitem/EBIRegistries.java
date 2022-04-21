package team.ebi.epicbanitem;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.Trigger;
import team.ebi.epicbanitem.api.Triggers;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunction;
import team.ebi.epicbanitem.api.expression.QueryExpressions;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateExpressions;
import team.ebi.epicbanitem.rule.RestrictionRulesStorage;

@Singleton
public class EBIRegistries {
  public static DefaultedRegistryType<Trigger> TRIGGER;
  public static DefaultedRegistryType<QueryExpressionFunction> QUERY_EXPRESSION;
  public static DefaultedRegistryType<UpdateExpressionFunction> UPDATE_EXPRESSION;

  public static DefaultedRegistryType<RestrictionRule> RESTRICTION_RULE;

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
            .register(EpicBanItem.key("trigger"), false, () -> Triggers.DEFAULT_REGISTRIES)
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

    RESTRICTION_RULE =
        event.register(EpicBanItem.key("restriction_rule"), true).asDefaultedType(Sponge::server);
  }

  @Listener(order = Order.POST)
  public void onRegisterRegistryValue(RegisterRegistryValueEvent.EngineScoped<Server> event) {
    QueryExpressions.EXPRESSIONS = QueryExpressions.toMap();
    UpdateExpressions.EXPRESSIONS = UpdateExpressions.toMap();
  }
}
