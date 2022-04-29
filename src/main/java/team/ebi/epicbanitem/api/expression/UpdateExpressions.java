package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.expression.ObjectUpdateExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;
import team.ebi.epicbanitem.expression.update.IncUpdateExpression;
import team.ebi.epicbanitem.expression.update.MulUpdateExpression;
import team.ebi.epicbanitem.expression.update.PopUpdateExpression;
import team.ebi.epicbanitem.expression.update.PullUpdateExpression;
import team.ebi.epicbanitem.expression.update.RenameUpdateExpression;
import team.ebi.epicbanitem.expression.update.SetUpdateExpression;
import team.ebi.epicbanitem.expression.update.UnsetUpdateExpression;

@RegistryScopes(scopes = RegistryScope.ENGINE)
public class UpdateExpressions {
  public static final DefaultedRegistryReference<UpdateExpressionFunction> SET =
      key(EpicBanItem.key(ExpressionKeys.SET));
  public static final DefaultedRegistryReference<UpdateExpressionFunction> UNSET =
      key(EpicBanItem.key(ExpressionKeys.UNSET));
  public static final DefaultedRegistryReference<UpdateExpressionFunction> RENAME =
      key(EpicBanItem.key(ExpressionKeys.RENAME));

  public static final DefaultedRegistryReference<UpdateExpressionFunction> POP =
      key(EpicBanItem.key(ExpressionKeys.POP));
  public static final DefaultedRegistryReference<UpdateExpressionFunction> PULL =
      key(EpicBanItem.key(ExpressionKeys.PULL));

  public static final DefaultedRegistryReference<UpdateExpressionFunction> INC =
      key(EpicBanItem.key(ExpressionKeys.INC));
  public static final DefaultedRegistryReference<UpdateExpressionFunction> MUL =
      key(EpicBanItem.key(ExpressionKeys.MUL));

  public static ImmutableMap<String, UpdateExpressionFunction> EXPRESSIONS;

  public static Map<ResourceKey, UpdateExpressionFunction> DEFAULT_REGISTRIES =
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
          .build();

  public static Registry<UpdateExpressionFunction> registry() {
    return EBIRegistries.UPDATE_EXPRESSION.get();
  }

  /**
   * @return The map without duplicated expression key. For example, there is <code>foo:$or</code>
   *     and <code>bar:$or</code>. The first should be accepted.
   */
  public static ImmutableMap<String, UpdateExpressionFunction> toMap() {
    return registry()
        .streamEntries()
        .reduce(
            ImmutableMap.<String, UpdateExpressionFunction>builder(),
            (builder, entry) -> builder.put(entry.key().value(), entry.value()),
            (builder, other) -> other)
        .build();
  }

  private static DefaultedRegistryReference<UpdateExpressionFunction> key(
      final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.UPDATE_EXPRESSION, location)
        .asDefaultedReference(Sponge::server);
  }

  static {
    Sponge.dataManager()
        .registerBuilder(RootUpdateExpression.class, new RootUpdateExpression.Builder());
  }
}
