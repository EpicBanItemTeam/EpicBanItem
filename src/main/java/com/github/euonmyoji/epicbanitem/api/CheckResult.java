package com.github.euonmyoji.epicbanitem.api;

import com.github.euonmyoji.epicbanitem.check.CheckRule;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

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

        public CheckResult.Banned updateBy(Function<? super DataView, ? extends DataView> function) {
            return new Banned(true, function.apply(this.view), banRules);
        }
    }
}
