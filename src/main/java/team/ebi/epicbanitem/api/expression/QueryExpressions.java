package team.ebi.epicbanitem.api.expression;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.expression.ArrayableQueryExpression;
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

public class QueryExpressions {
  public static final DefaultedRegistryReference<QueryExpressionFunction> OR =
      key(EpicBanItem.key(ExpressionKeys.OR));
  public static final DefaultedRegistryReference<QueryExpressionFunction> NOR =
      key(EpicBanItem.key(ExpressionKeys.NOR));
  public static final DefaultedRegistryReference<QueryExpressionFunction> AND =
      key(EpicBanItem.key(ExpressionKeys.AND));
  public static final DefaultedRegistryReference<QueryExpressionFunction> NOT =
      key(EpicBanItem.key(ExpressionKeys.NOT));
  public static final DefaultedRegistryReference<QueryExpressionFunction> EQ =
      key(EpicBanItem.key(ExpressionKeys.EQ));
  public static final DefaultedRegistryReference<QueryExpressionFunction> NE =
      key(EpicBanItem.key(ExpressionKeys.NE));
  public static final DefaultedRegistryReference<QueryExpressionFunction> GT =
      key(EpicBanItem.key(ExpressionKeys.GT));
  public static final DefaultedRegistryReference<QueryExpressionFunction> LT =
      key(EpicBanItem.key(ExpressionKeys.LT));
  public static final DefaultedRegistryReference<QueryExpressionFunction> GTE =
      key(EpicBanItem.key(ExpressionKeys.GTE));
  public static final DefaultedRegistryReference<QueryExpressionFunction> LTE =
      key(EpicBanItem.key(ExpressionKeys.LTE));
  public static final DefaultedRegistryReference<QueryExpressionFunction> IN =
      key(EpicBanItem.key(ExpressionKeys.IN));
  public static final DefaultedRegistryReference<QueryExpressionFunction> NIN =
      key(EpicBanItem.key(ExpressionKeys.NIN));
  public static final DefaultedRegistryReference<QueryExpressionFunction> SIZE =
      key(EpicBanItem.key(ExpressionKeys.SIZE));
  public static final DefaultedRegistryReference<QueryExpressionFunction> ALL =
      key(EpicBanItem.key(ExpressionKeys.ALL));
  public static final DefaultedRegistryReference<QueryExpressionFunction> ELEM_MATCH =
      key(EpicBanItem.key(ExpressionKeys.ELEM_MATCH));
  public static final DefaultedRegistryReference<QueryExpressionFunction> EXISTS =
      key(EpicBanItem.key(ExpressionKeys.EXISTS));
  public static final DefaultedRegistryReference<QueryExpressionFunction> REGEX =
      key(EpicBanItem.key(ExpressionKeys.REGEX));

  public static ImmutableMap<String, QueryExpressionFunction> EXPRESSIONS;

  public static Map<ResourceKey, QueryExpressionFunction> DEFAULT_REGISTRIES =
      ImmutableMap.<ResourceKey, QueryExpressionFunction>builder()
          // Logical
          .put(EpicBanItem.key(ExpressionKeys.OR), OrQueryExpression::new)
          .put(EpicBanItem.key(ExpressionKeys.NOR), NorQueryExpression::new)
          .put(EpicBanItem.key(ExpressionKeys.AND), AndQueryExpression::new)
          .put(EpicBanItem.key(ExpressionKeys.NOT), NotQueryExpression::new)
          // Compare
          .put(
              EpicBanItem.key(ExpressionKeys.EQ),
              (view) -> new ArrayableQueryExpression(new EqQueryExpression(view)))
          .put(
              EpicBanItem.key(ExpressionKeys.NE),
              (view) -> new ArrayableQueryExpression(new NeQueryExpression(view)))
          .put(
              EpicBanItem.key(ExpressionKeys.GT),
              (view) -> new ArrayableQueryExpression(new GtQueryExpression(view)))
          .put(
              EpicBanItem.key(ExpressionKeys.LT),
              (view) -> new ArrayableQueryExpression(new LtQueryExpression(view)))
          .put(
              EpicBanItem.key(ExpressionKeys.GTE),
              (view) -> new ArrayableQueryExpression(new GteQueryExpression(view)))
          .put(
              EpicBanItem.key(ExpressionKeys.LTE),
              (view) -> new ArrayableQueryExpression(new LteQueryExpression(view)))
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
          .build();

  public static Registry<QueryExpressionFunction> registry() {
    return Sponge.server().registry(EBIRegistries.PREDICATE_EXPRESSION);
  }

  /**
   * @return The map without duplicated expression key. For example, there is <code>foo:$or</code>
   *     and <code>bar:$or</code>. The first should be accepted.
   */
  public static ImmutableMap<String, QueryExpressionFunction> toMap() {
    return registry()
        .streamEntries()
        .reduce(
            ImmutableMap.<String, QueryExpressionFunction>builder(),
            (builder, entry) -> builder.put(entry.key().value(), entry.value()),
            (builder, other) -> other)
        .build();
  }

  private static DefaultedRegistryReference<QueryExpressionFunction> key(
      final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.PREDICATE_EXPRESSION, location)
        .asDefaultedReference(Sponge::server);
  }
}
