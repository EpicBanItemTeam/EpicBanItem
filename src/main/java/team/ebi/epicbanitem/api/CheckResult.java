package team.ebi.epicbanitem.api;

import com.google.common.collect.ImmutableList;
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
 * @author The EpicBanItem Team
 */
@NonnullByDefault
public class CheckResult {
    protected final DataView view;

    private CheckResult(DataView view) {
        this.view = view;
    }

    public boolean isUpdateNeeded() {
        return false;
    }

    public boolean isBanned() {
        return this instanceof CheckResult.Banned;
    }

    public Optional<DataContainer> getFinalView() {
        return Optional.empty();
    }

    public CheckResult banFor(Predicate<? super DataView> predicate) {
        return predicate.test(this.view) ? new Banned(false, this.view, ImmutableList.of()) : this;
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

        @Deprecated // not a stable api yet
        public List<Tuple<Text, Optional<String>>> getBanRules() {
            return this.banRules;
        }

        @Override
        public boolean isUpdateNeeded() {
            return this.updated;
        }

        @Override
        public Optional<DataContainer> getFinalView() {
            return this.updated ? Optional.of(this.view.copy()) : Optional.empty();
        }

        @Override
        public CheckResult.Banned banFor(Predicate<? super DataView> predicate) {
            predicate.test(view); //I do not like side effects
            return this;
        }

        public CheckResult.Banned withMessage(Text text) {
            return new Banned(
                updated,
                view,
                ImmutableList.<Tuple<Text, Optional<String>>>builder().addAll(banRules).add(Tuple.of(text, Optional.empty())).build()
            );
        }

        public CheckResult.Banned withMessage(Text text, String customMessageTemplate) {
            return new Banned(
                updated,
                view,
                ImmutableList
                    .<Tuple<Text, Optional<String>>>builder()
                    .addAll(banRules)
                    .add(Tuple.of(text, Optional.of(customMessageTemplate)))
                    .build()
            );
        }

        public CheckResult.Banned updateBy(Function<? super DataView, ? extends DataView> function) {
            return new Banned(true, function.apply(this.view), banRules);
        }
    }
}
