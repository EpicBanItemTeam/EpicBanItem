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
import team.ebi.epicbanitem.expression.FlexiblePredicateExpression;
import team.ebi.epicbanitem.expression.predicate.AllPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.AndPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.ElemMatchPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.EqPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.ExistsPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.GtPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.GtePredicateExpression;
import team.ebi.epicbanitem.expression.predicate.InPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.LtPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.LtePredicateExpression;
import team.ebi.epicbanitem.expression.predicate.NePredicateExpression;
import team.ebi.epicbanitem.expression.predicate.NinPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.NorPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.NotPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.OrPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.RegexPredicateExpression;
import team.ebi.epicbanitem.expression.predicate.SizePredicateExpression;

public class PredicateExpressions {
  public static final DefaultedRegistryReference<PredicateExpressionFunction> OR =
      key(EpicBanItem.key(PredicateExpressionKeys.OR));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> NOR =
      key(EpicBanItem.key(PredicateExpressionKeys.NOR));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> AND =
      key(EpicBanItem.key(PredicateExpressionKeys.AND));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> NOT =
      key(EpicBanItem.key(PredicateExpressionKeys.NOT));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> EQ =
      key(EpicBanItem.key(PredicateExpressionKeys.EQ));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> NE =
      key(EpicBanItem.key(PredicateExpressionKeys.NE));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> GT =
      key(EpicBanItem.key(PredicateExpressionKeys.GT));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> LT =
      key(EpicBanItem.key(PredicateExpressionKeys.LT));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> GTE =
      key(EpicBanItem.key(PredicateExpressionKeys.GTE));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> LTE =
      key(EpicBanItem.key(PredicateExpressionKeys.LTE));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> IN =
      key(EpicBanItem.key(PredicateExpressionKeys.IN));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> NIN =
      key(EpicBanItem.key(PredicateExpressionKeys.NIN));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> SIZE =
      key(EpicBanItem.key(PredicateExpressionKeys.SIZE));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> ALL =
      key(EpicBanItem.key(PredicateExpressionKeys.ALL));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> ELEM_MATCH =
      key(EpicBanItem.key(PredicateExpressionKeys.ELEM_MATCH));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> EXISTS =
      key(EpicBanItem.key(PredicateExpressionKeys.EXISTS));
  public static final DefaultedRegistryReference<PredicateExpressionFunction> REGEX =
      key(EpicBanItem.key(PredicateExpressionKeys.REGEX));

  public static ImmutableMap<String, PredicateExpressionFunction> EXPRESSIONS;

  public static Map<ResourceKey, PredicateExpressionFunction> DEFAULT_REGISTRIES =
      ImmutableMap.<ResourceKey, PredicateExpressionFunction>builder()
          // Logical
          .put(EpicBanItem.key(PredicateExpressionKeys.OR), OrPredicateExpression::new)
          .put(EpicBanItem.key(PredicateExpressionKeys.NOR), NorPredicateExpression::new)
          .put(EpicBanItem.key(PredicateExpressionKeys.AND), AndPredicateExpression::new)
          .put(EpicBanItem.key(PredicateExpressionKeys.NOT), NotPredicateExpression::new)
          // Compare
          .put(
              EpicBanItem.key(PredicateExpressionKeys.EQ),
              (view) -> new FlexiblePredicateExpression(new EqPredicateExpression(view)))
          .put(
              EpicBanItem.key(PredicateExpressionKeys.NE),
              (view) -> new FlexiblePredicateExpression(new NePredicateExpression(view)))
          .put(
              EpicBanItem.key(PredicateExpressionKeys.GT),
              (view) -> new FlexiblePredicateExpression(new GtPredicateExpression(view)))
          .put(
              EpicBanItem.key(PredicateExpressionKeys.LT),
              (view) -> new FlexiblePredicateExpression(new LtPredicateExpression(view)))
          .put(
              EpicBanItem.key(PredicateExpressionKeys.GTE),
              (view) -> new FlexiblePredicateExpression(new GtePredicateExpression(view)))
          .put(
              EpicBanItem.key(PredicateExpressionKeys.LTE),
              (view) -> new FlexiblePredicateExpression(new LtePredicateExpression(view)))
          // In
          .put(EpicBanItem.key(PredicateExpressionKeys.IN), InPredicateExpression::new)
          .put(EpicBanItem.key(PredicateExpressionKeys.NIN), NinPredicateExpression::new)
          // Array
          .put(EpicBanItem.key(PredicateExpressionKeys.SIZE), SizePredicateExpression::new)
          .put(EpicBanItem.key(PredicateExpressionKeys.ALL), AllPredicateExpression::new)
          .put(
              EpicBanItem.key(PredicateExpressionKeys.ELEM_MATCH),
              ElemMatchPredicateExpression::new)
          // Other
          .put(EpicBanItem.key(PredicateExpressionKeys.EXISTS), ExistsPredicateExpression::new)
          .put(EpicBanItem.key(PredicateExpressionKeys.REGEX), RegexPredicateExpression::new)
          .build();

  public static Registry<PredicateExpressionFunction> registry() {
    return Sponge.server().registry(EBIRegistries.PREDICATE_EXPRESSION);
  }

  /**
   * @return The map without duplicated expression key. For example, there is <code>foo:$or</code>
   *     and <code>bar:$or</code>. The first should be accepted.
   */
  public static ImmutableMap<String, PredicateExpressionFunction> toMap() {
    return registry()
        .streamEntries()
        .reduce(
            ImmutableMap.<String, PredicateExpressionFunction>builder(),
            (builder, entry) -> builder.put(entry.key().value(), entry.value()),
            (builder, other) -> other)
        .build();
  }

  private static DefaultedRegistryReference<PredicateExpressionFunction> key(
      final ResourceKey location) {
    return RegistryKey.of(EBIRegistries.PREDICATE_EXPRESSION, location)
        .asDefaultedReference(Sponge::server);
  }
}
