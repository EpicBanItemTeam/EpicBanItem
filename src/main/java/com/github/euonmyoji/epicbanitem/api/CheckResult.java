package com.github.euonmyoji.epicbanitem.api;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.util.TextUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CheckResult {
    private static final Map<String, TextTemplate> customInfoMessageCache = new ConcurrentHashMap<>();
    private static final Set<String> INFO_TOKENS = ImmutableSet.of("rules", "trigger", "item_pre", "item_post");

    protected final DataView view;

    private CheckResult(DataView view) {
        this.view = view;
    }

    public boolean isBanned() {
        return this instanceof CheckResult.Banned;
    }

    public Optional<DataContainer> getFinalView() {
        return Optional.empty();
    }

    @Deprecated
    public CheckResult banFor(Predicate<? super DataView> predicate) {
        return predicate.test(this.view) ? new Banned(false, this.view, ImmutableList.of()) : this;
    }

    public CheckResult banFor(Predicate<? super DataView> predicate, Text text, @Nullable String customMessage) {
        return predicate.test(this.view) ? new Banned(false, this.view, ImmutableList.of(Tuple.of(text, Optional.ofNullable(customMessage)))) : this;
    }

    public CheckResult banFor(Predicate<? super DataView> predicate, Function<? super DataView, ? extends DataView> update, Text text, @Nullable String customMessage) {
        List<Tuple<Text, Optional<String>>> rules = ImmutableList.of(Tuple.of(text, Optional.ofNullable(customMessage)));
        return predicate.test(this.view) ? new Banned(true, update.apply(this.view), rules) : this;
    }

    public Collection<Text> prepareMessage(CheckRuleTrigger trigger, Text itemPre, Text itemPost) {
        return Collections.emptyList();
    }

    public static CheckResult empty(DataContainer view) {
        return new CheckResult(view);
    }

    @NonnullByDefault
    public static class Banned extends CheckResult {
        private final boolean updated;
        private final List<Tuple<Text, Optional<String>>> banRules;

        private Banned(boolean updated, DataView view, List<Tuple<Text, Optional<String>>> banRules) {
            super(view);
            this.updated = updated;
            this.banRules = banRules;
        }

        @Override
        public Optional<DataContainer> getFinalView() {
            return this.updated ? Optional.of(this.view.copy()) : Optional.empty();
        }

        @Deprecated
        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate) {
            predicate.test(view);//I do not like side effects
            return this;
        }


        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate, Text text, @Nullable String customMessage) {
            if (predicate.test(view)) {
                return new Banned(updated, view,
                        ImmutableList.<Tuple<Text, Optional<String>>>builder()
                                .addAll(banRules).add(Tuple.of(text, Optional.ofNullable(customMessage))).build()
                );
            } else {
                return this;
            }
        }

        @Override
        public CheckResult.Banned banFor(
                Predicate<? super DataView> predicate,
                Function<? super DataView, ? extends DataView> update,
                Text text, @Nullable String customMessage
        ) {
            if (predicate.test(view)) {
                return new Banned(
                        true,
                        update.apply(view),
                        ImmutableList.<Tuple<Text, Optional<String>>>builder()
                                .addAll(banRules)
                                .add(Tuple.of(text, Optional.ofNullable(customMessage)))
                                .build()
                );
            } else {
                return this;
            }
        }

        @Override
        public Collection<Text> prepareMessage(CheckRuleTrigger trigger, Text itemPre, Text itemPost) {
            LinkedHashMap<String, Tuple<TextTemplate, List<Text>>> map = new LinkedHashMap<>();
            List<Text> undefined = new ArrayList<>();
            for (Tuple<Text, Optional<String>> rule : banRules) {
                if (rule.getSecond().isPresent()) {
                    map.computeIfAbsent(
                            rule.getSecond().get(),
                            s -> new Tuple<>(
                                    customInfoMessageCache.computeIfAbsent(s, s1 -> TextUtil.parseTextTemplate(s1, INFO_TOKENS)),
                                    new ArrayList<>()
                            )
                    ).getSecond().add(rule.getFirst());
                } else {
                    undefined.add(rule.getFirst());
                }
            }
            Function<List<Text>, Map<String, Text>> toParams = checkRules -> ImmutableMap.of(
                    "rules", Text.joinWith(Text.of(","), checkRules),
                    "trigger", trigger.toText(),
                    "item_pre", itemPre,
                    "item_post", itemPre
            );
            List<Text> result = new ArrayList<>();
            if (!undefined.isEmpty()) {
                result.add(EpicBanItem.getMessages().getMessage(
                        updated ? "epicbanitem.info.defaultUpdateMessage" : "epicbanitem.info.defaultBanMessage",
                        toParams.apply(undefined)
                ));
            }
            for (Tuple<TextTemplate, List<Text>> tuple : map.values()) {
                result.add(tuple.getFirst().apply(toParams.apply(tuple.getSecond())).build());
            }
            return result.stream().filter(text -> !text.isEmpty()).collect(Collectors.toList());
        }

        public CheckResult.Banned updateBy(Function<? super DataView, ? extends DataView> function) {
            return new Banned(true, function.apply(this.view), banRules);
        }
    }
}
