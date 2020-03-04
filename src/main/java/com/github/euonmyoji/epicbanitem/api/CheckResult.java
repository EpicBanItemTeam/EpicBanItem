package com.github.euonmyoji.epicbanitem.api;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 * @deprecated since it has been renamed
 * @see team.ebi.epicbanitem.api.CheckResult
 */
@Deprecated
@NonnullByDefault
@SuppressWarnings("unused")
public class CheckResult {
    protected final team.ebi.epicbanitem.api.CheckResult proxy;

    CheckResult(team.ebi.epicbanitem.api.CheckResult proxy) {
        this.proxy = proxy;
    }

    public boolean isUpdateNeeded() {
        return this.proxy.isUpdateNeeded();
    }

    public boolean isBanned() {
        return this.proxy.isBanned();
    }

    public Optional<DataContainer> getFinalView() {
        return this.proxy.getFinalView();
    }

    public CheckResult banFor(Predicate<? super DataView> predicate) {
        return new CheckResult(this.proxy.banFor(predicate));
    }

    public static CheckResult empty(DataContainer view) {
        return new CheckResult(team.ebi.epicbanitem.api.CheckResult.empty(view));
    }

    @Deprecated
    @NonnullByDefault
    public static class Banned extends CheckResult {
        protected final team.ebi.epicbanitem.api.CheckResult.Banned proxy;

        private Banned(team.ebi.epicbanitem.api.CheckResult.Banned proxy) {
            super(proxy);
            this.proxy = proxy;
        }

        @Deprecated // not a stable api yet
        public List<Tuple<Text, Optional<String>>> getBanRules() {
            return this.proxy.getBanRules();
        }

        @Override
        public boolean isUpdateNeeded() {
            return this.proxy.isUpdateNeeded();
        }

        @Override
        public Optional<DataContainer> getFinalView() {
            return this.proxy.getFinalView();
        }

        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate) {
            return new Banned(this.proxy.banFor(predicate));
        }

        public CheckResult.Banned withMessage(Text text) {
            return new Banned(this.proxy.withMessage(text));
        }

        public CheckResult.Banned withMessage(Text text, String customMessageTemplate) {
            return new Banned(this.proxy.withMessage(text, customMessageTemplate));
        }

        public CheckResult.Banned updateBy(Function<? super DataView, ? extends DataView> function) {
            return new Banned(this.proxy.updateBy(function));
        }
    }
}
