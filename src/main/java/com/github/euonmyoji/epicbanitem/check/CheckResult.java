package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
public class CheckResult {
    private final DataView view;
    private final boolean banned;
    private final boolean updated;
    private final Stream<CheckRule> breakRules;

    private CheckResult(Stream<CheckRule> breakRules, boolean updated, boolean banned, DataView view) {
        this.breakRules = breakRules;
        this.updated = updated;
        this.banned = banned;
        this.view = view;
    }

    public static CheckResult empty(DataContainer view) {
        return new CheckResult(Stream.empty(), false, false, view);
    }

    public static CheckResult concat(CheckResult parent, CheckRule rule) {
        return new CheckResult(Stream.concat(parent.breakRules, Stream.of(rule)), parent.updated, true, parent.view);
    }

    public static CheckResult concat(CheckResult parent, CheckRule rule, DataView newView) {
        return new CheckResult(Stream.concat(parent.breakRules, Stream.of(rule)), true, true, newView);
    }

    public boolean isBanned() {
        return this.banned;
    }

    public Stream<CheckRule> getBreakRules() {
        return this.breakRules;
    }

    public Optional<DataContainer> getFinalView() {
        return this.updated ? Optional.of(this.view.copy()) : Optional.empty();
    }

    public DataContainer getFinalViewUnchecked() {
        return this.view.copy();
    }
}
