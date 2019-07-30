package com.github.euonmyoji.epicbanitem.api;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.annotation.NonnullByDefault;

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

    public CheckResult banFor(Predicate<? super DataView> predicate) {
        return predicate.test(this.view) ? new Banned(false, this.view) : this;
    }

    public static CheckResult empty(DataContainer view) {
        return new CheckResult(view);
    }

    @NonnullByDefault
    public static class Banned extends CheckResult {
        private final boolean updated;

        private Banned(boolean updated, DataView view) {
            super(view);
            this.updated = updated;
        }

        @Override
        public Optional<DataContainer> getFinalView() {
            return this.updated ? Optional.of(this.view.copy()) : Optional.empty();
        }

        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate) {
            return this;
        }

        public CheckResult updateBy(Function<? super DataView, ? extends DataView> function) {
            return new Banned(true, function.apply(this.view));
        }
    }
}
