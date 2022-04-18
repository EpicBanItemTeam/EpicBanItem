package team.ebi.epicbanitem;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.plugin.PluginContainer;
import team.ebi.epicbanitem.api.Trigger;
import team.ebi.epicbanitem.api.Triggers;
import team.ebi.epicbanitem.api.expression.QueryExpressionFunction;
import team.ebi.epicbanitem.api.expression.QueryExpressions;
import team.ebi.epicbanitem.api.expression.UpdateExpressionFunction;
import team.ebi.epicbanitem.api.expression.UpdateExpressions;

@Singleton
public class EBIRegistries {
  public static RegistryType<Trigger> TRIGGER;
  public static RegistryType<QueryExpressionFunction> QUERY_EXPRESSION;
  public static RegistryType<UpdateExpressionFunction> UPDATE_EXPRESSION;

  public static void registerRegistries(final RegisterRegistryEvent event) {
    TRIGGER = event.register(EpicBanItem.key("trigger"), false, () -> Triggers.DEFAULT_REGISTRIES);

    QUERY_EXPRESSION =
        event.register(
            EpicBanItem.key("query_expression"), false, () -> QueryExpressions.DEFAULT_REGISTRIES);

    UPDATE_EXPRESSION =
        event.register(
            EpicBanItem.key("update_expression"),
            false,
            () -> UpdateExpressions.DEFAULT_REGISTRIES);
  }

  @Inject
  public EBIRegistries(PluginContainer pluginContainer, EventManager eventManager) {
    eventManager.registerListener(
        EventListenerRegistration.builder(
                new TypeToken<RegisterRegistryEvent.EngineScoped<Server>>() {})
            .plugin(pluginContainer)
            .listener(EBIRegistries::registerRegistries)
            .build());

    eventManager.registerListener(
        EventListenerRegistration.builder(
                new TypeToken<RegisterRegistryValueEvent.EngineScoped<Server>>() {})
            .plugin(pluginContainer)
            .order(Order.POST)
            .listener(
                event -> {
                  QueryExpressions.EXPRESSIONS = QueryExpressions.toMap();
                  UpdateExpressions.EXPRESSIONS = UpdateExpressions.toMap();
                })
            .build());
  }
}
