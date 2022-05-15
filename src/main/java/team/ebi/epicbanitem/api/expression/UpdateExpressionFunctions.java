package team.ebi.epicbanitem.api.expression;

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
import team.ebi.epicbanitem.expression.RootUpdateExpression;

@RegistryScopes(scopes = RegistryScope.ENGINE)
public final class UpdateExpressionFunctions {

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

  public static Map<String, UpdateExpressionFunction> expressions;

  static {
    Sponge.dataManager()
        .registerBuilder(RootUpdateExpression.class, new RootUpdateExpression.Builder());
  }

  private UpdateExpressionFunctions() {
  }

  public static Registry<UpdateExpressionFunction> registry() {
    return EBIRegistries.UPDATE_EXPRESSION.get();
  }

  private static DefaultedRegistryReference<UpdateExpressionFunction> key(
      final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.UPDATE_EXPRESSION, location)
        .asDefaultedReference(Sponge::server);
  }
}
