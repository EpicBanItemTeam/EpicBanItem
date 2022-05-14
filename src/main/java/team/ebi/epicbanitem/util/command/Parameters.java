package team.ebi.epicbanitem.util.command;

import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.util.StartsWithPredicate;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionPreset;
import team.ebi.epicbanitem.api.RestrictionRule;
import team.ebi.epicbanitem.api.RestrictionRules;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;
import team.ebi.epicbanitem.rule.RestrictionRuleImpl;
import team.ebi.epicbanitem.util.DataSerializableValueParser;

public final class Parameters {

  public static final Parameter.Value.Builder<ResourceKey> RULE_NAME =
      Parameter.builder(ResourceKey.class)
          .addParser(
              new ValueParser<>() {
                @Override
                public Optional<? extends ResourceKey> parseValue(
                    Key<? super ResourceKey> parameterKey, Mutable reader, Builder context)
                    throws ArgumentParseException {
                  ResourceKey key = reader.parseResourceKey(EpicBanItem.NAMESPACE);
                  if (!key.namespace().equals(EpicBanItem.NAMESPACE)) {
                    throw new ArgumentParseException(
                        Component.translatable("epicbanitem.command.create.rejectNamespace"),
                        key.toString(),
                        key.namespace().length());
                  }
                  return Optional.of(key);
                }

                @Override
                public List<ClientCompletionType> clientCompletionType() {
                  return List.of(ClientCompletionTypes.RESOURCE_KEY.get());
                }
              });
  public static final Parameter.Value.Builder<ResourceKey> RULE_KEY =
      Parameter.resourceKey()
          .completer(
              (context, currentInput) ->
                  RestrictionRules.keyStream()
                      .map(
                          it ->
                              it.namespace().equals(EpicBanItem.NAMESPACE)
                                  ? it.value()
                                  : it.asString())
                      .filter(it -> new StartsWithPredicate(it).test(currentInput))
                      .map(CommandCompletion::of)
                      .collect(Collectors.toList()));

  public static final Parameter.Value.Builder<RestrictionPreset> PRESET =
      Parameter.registryElement(
          TypeToken.get(RestrictionPreset.class), EBIRegistries.PRESET, EpicBanItem.NAMESPACE);

  public static final Parameter.Value.Builder<RestrictionRule> RULE =
      Parameter.builder(Keys.RULE)
          .addParser(new DataSerializableValueParser<>(RestrictionRuleImpl.class));

  public static final Parameter.Value.Builder<RootQueryExpression> QUERY =
      Parameter.builder(Keys.QUERY)
          .addParser(new DataSerializableValueParser<>(RootQueryExpression.class));

  public static final Parameter.Value.Builder<RootUpdateExpression> UPDATE =
      Parameter.builder(Keys.UPDATE)
          .addParser(new DataSerializableValueParser<>(RootUpdateExpression.class));

  public static final class Keys {

    public static final Key<RootQueryExpression> QUERY =
        Parameter.key("query", RootQueryExpression.class);
    public static final Key<RootUpdateExpression> UPDATE =
        Parameter.key("update", RootUpdateExpression.class);
    public static final Key<ResourceKey> RULE_KEY = Parameter.key("rule-key", ResourceKey.class);

    public static final Key<ResourceKey> RULE_NAME = Parameter.key("rule-name", ResourceKey.class);
    public static final Key<RestrictionRule> RULE = Parameter.key("rule", RestrictionRule.class);
    public static final Key<RestrictionPreset> PRESET =
        Parameter.key("preset", RestrictionPreset.class);
  }
}
