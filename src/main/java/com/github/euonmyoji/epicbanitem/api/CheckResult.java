package com.github.euonmyoji.epicbanitem.api;

import com.github.euonmyoji.epicbanitem.EpicBanItem;
import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CheckResult {
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

    public List<CheckRule> getBanRules() {
        return Collections.emptyList();
    }

    public CheckResult banFor(Predicate<? super DataView> predicate, CheckRule rule) {
        return predicate.test(this.view) ? new Banned(false, this.view, ImmutableList.of(rule)) : this;
    }

    public CheckResult banFor(Predicate<? super DataView> predicate, CheckRule rule, Function<? super DataView, ? extends DataView> update) {
        return predicate.test(this.view) ? new Banned(true, update.apply(this.view), ImmutableList.of(rule)) : this;
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
        private final List<CheckRule> banRules;

        private Banned(boolean updated, DataView view, List<CheckRule> banRules) {
            super(view);
            this.updated = updated;
            this.banRules = banRules;
        }

        @Override
        public Optional<DataContainer> getFinalView() {
            return this.updated ? Optional.of(this.view.copy()) : Optional.empty();
        }

        @Override
        public List<CheckRule> getBanRules() {
            return banRules;
        }

        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate, CheckRule rule) {
            if (predicate.test(view)) {
                return new Banned(updated, view, ImmutableList.<CheckRule>builder().addAll(banRules).add(rule).build());
            } else {
                return this;
            }
        }

        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate, CheckRule rule, Function<? super DataView, ? extends DataView> update) {
            if (predicate.test(view)) {
                return new Banned(true, update.apply(view), ImmutableList.<CheckRule>builder().addAll(banRules).add(rule).build());
            } else {
                return this;
            }
        }

        @Override
        public Collection<Text> prepareMessage(CheckRuleTrigger trigger, Text itemPre, Text itemPost) {
            LinkedHashMap<String, Tuple<TextTemplate, List<CheckRule>>> map = new LinkedHashMap<>();
            List<CheckRule> undefined = new ArrayList<>();
            for (CheckRule rule : banRules) {
                if (rule.getCustomMessageString().isPresent()) {
                    //noinspection ConstantConditions
                    map.computeIfAbsent(rule.getCustomMessageString().get(), s -> new Tuple<>(rule.getCustomMessage(), new ArrayList<>()))
                            .getSecond().add(rule);
                } else {
                    undefined.add(rule);
                }
            }
            Function<List<CheckRule>, Map<String, Text>> toParams = checkRules -> ImmutableMap.of(
                    "rules", Text.joinWith(Text.of(","), checkRules.stream().map(CheckRule::toText).collect(Collectors.toList())),
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
            for (Tuple<TextTemplate, List<CheckRule>> tuple : map.values()) {
                result.add(tuple.getFirst().apply(toParams.apply(tuple.getSecond())).build());
            }
            return result.stream().filter(text -> !text.isEmpty()).collect(Collectors.toList());
        }

        public CheckResult.Banned updateBy(Function<? super DataView, ? extends DataView> function) {
            return new Banned(true, function.apply(this.view), banRules);
        }
    }
}
