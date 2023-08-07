/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.util.command;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.spongepowered.api.world.server.ServerWorld;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import team.ebi.epicbanitem.EBIRegistries;
import team.ebi.epicbanitem.EpicBanItem;
import team.ebi.epicbanitem.api.RestrictionPreset;
import team.ebi.epicbanitem.api.rule.RestrictionRule;
import team.ebi.epicbanitem.api.rule.RestrictionRuleService;
import team.ebi.epicbanitem.api.rule.RulePredicateService;
import team.ebi.epicbanitem.api.trigger.RestrictionTrigger;
import team.ebi.epicbanitem.expression.RootQueryExpression;
import team.ebi.epicbanitem.expression.RootUpdateExpression;

@Singleton
public final class Parameters {

    public final Parameter.Value.Builder<ResourceKey> ruleName;
    public final Parameter.Value.Builder<ResourceKey> ruleKey;
    public final Parameter.Value.Builder<RestrictionPreset> preset;
    public final Parameter.Value.Builder<RestrictionTrigger> trigger;
    public final Parameter.Value.Builder<ServerWorld> world;
    public final Parameter.Value.Builder<RestrictionRule> rule;
    public final Parameter.Value.Builder<RootQueryExpression> query;
    public final Parameter.Value.Builder<RootUpdateExpression> update;
    public final Parameter.Value.Builder<ResourceKey> predicate;

    @Inject
    public Parameters(
            Keys keys,
            RestrictionRuleService ruleService,
            RulePredicateService predicateService,
            UpdateExpressionValueParser updateExpressionValueParser,
            QueryExpressionValueParser queryExpressionValueParser) {
        ruleName = Parameter.builder(ResourceKey.class).addParser(new ValueParser<>() {
            @Override
            public Optional<? extends ResourceKey> parseValue(
                    Key<? super ResourceKey> parameterKey, Mutable reader, Builder context)
                    throws ArgumentParseException {
                ResourceKey key = reader.parseResourceKey(EpicBanItem.NAMESPACE);
                if (key.value().length() == 0) {
                    return Optional.empty();
                }
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
                return Lists.newArrayList(ClientCompletionTypes.RESOURCE_KEY.get());
            }
        });
        ruleKey = Parameter.resourceKey()
                .addParser((key, reader, context) -> {
                    ResourceKey resourceKey = reader.parseResourceKey(EpicBanItem.NAMESPACE);
                    return ruleService.of(resourceKey).map(it -> resourceKey);
                })
                .completer((context, currentInput) -> ruleService
                        .keys()
                        .filter(it -> {
                            StartsWithPredicate starts = new StartsWithPredicate(currentInput);
                            return starts.test(it.value()) || starts.test(it.namespace());
                        })
                        .map(ResourceKey::asString)
                        .map(CommandCompletion::of)
                        .collect(Collectors.toList()));

        preset = Parameter.registryElement(
                TypeToken.get(RestrictionPreset.class), EBIRegistries.PRESET, EpicBanItem.NAMESPACE);

        trigger = Parameter.registryElement(
                TypeToken.get(RestrictionTrigger.class), EBIRegistries.TRIGGER, EpicBanItem.NAMESPACE);
        world = Parameter.world();

        rule = Parameter.builder(RestrictionRule.class)
                .addParser((key, reader, context) -> ruleService.of(reader.parseResourceKey(EpicBanItem.NAMESPACE)))
                .completer((context, currentInput) -> ruleService
                        .keys()
                        .filter(it -> {
                            StartsWithPredicate starts = new StartsWithPredicate(currentInput);
                            return starts.test(it.value()) || starts.test(it.namespace());
                        })
                        .map(ResourceKey::asString)
                        .map(CommandCompletion::of)
                        .collect(Collectors.toList()));

        query = Parameter.builder(keys.query).addParser(queryExpressionValueParser);
        update = Parameter.builder(keys.update).addParser(updateExpressionValueParser);

        predicate = Parameter.resourceKey()
                .addParser((key, reader, context) -> Optional.of(reader.parseResourceKey(EpicBanItem.NAMESPACE)))
                .completer((context, currentInput) -> predicateService
                        .predicates()
                        .filter(it -> {
                            StartsWithPredicate starts = new StartsWithPredicate(currentInput);
                            return starts.test(it.value()) || starts.test(it.namespace());
                        })
                        .map(ResourceKey::asString)
                        .map(CommandCompletion::of)
                        .collect(Collectors.toList()));
    }

    @Singleton
    public static final class Keys {
        public final Key<RootQueryExpression> query = Parameter.key("query", RootQueryExpression.class);
        public final Key<RootUpdateExpression> update = Parameter.key("update", RootUpdateExpression.class);
        public final Key<ResourceKey> ruleKey = Parameter.key("rule-key", ResourceKey.class);

        public final Key<ResourceKey> ruleName = Parameter.key("rule-name", ResourceKey.class);
        public final Key<RestrictionRule> rule = Parameter.key("rule", RestrictionRule.class);
        public final Key<RestrictionPreset> preset = Parameter.key("preset", RestrictionPreset.class);
        public final Key<RestrictionTrigger> trigger = Parameter.key("trigger", RestrictionTrigger.class);
        public final Key<ServerWorld> world = Parameter.key("world", ServerWorld.class);
        public final Key<ResourceKey> predicate = Parameter.key("predicate", ResourceKey.class);
    }
}
