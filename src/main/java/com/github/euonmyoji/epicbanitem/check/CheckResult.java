package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.data.DataView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author GINYAI yinyangshi
 * 检查物品的结果
 * the result of checkItemStack
 */
public class CheckResult {
    List<CheckRule> breakRules;
    boolean remove;
    DataView view;

    private CheckResult() {
        breakRules = new ArrayList<>();
        remove = false;
        view = null;
    }

    public List<CheckRule> getBreakRules() {
        return this.breakRules;
    }

    public boolean shouldRemove() {
        return this.remove;
    }

    public Optional<DataView> getFinalView() {
        return Optional.ofNullable(this.view);
    }

    public boolean isBanned() {
        return this.breakRules.size() > 0;
    }

    public static CheckResult empty() {
        return new CheckResult();
    }
}
