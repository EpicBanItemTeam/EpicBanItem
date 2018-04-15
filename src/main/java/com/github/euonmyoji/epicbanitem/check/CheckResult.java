package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

/**
 * @author yinyangshi
 * 检查物品的结果
 * the result of check
 */
public class CheckResult {
    private boolean banned;
    private Text message;
    private List<CheckRule> breakRules;

    public CheckResult(boolean banned, Text message, List<CheckRule> breakRules) {
        this.banned = banned;
        this.message = message;
        this.breakRules = breakRules;
    }

    public Optional<List<CheckRule>> getBreakRules() {
        return Optional.ofNullable(this.breakRules);
    }

    public Optional<Text> getMessage() {
        return Optional.ofNullable(this.message);
    }

    public boolean isBanned() {
        return this.banned;
    }


    public static CheckResultBuilder builder() {
        return new CheckResultBuilder();
    }

    public static class CheckResultBuilder {
        private boolean banned;
        private Text message;
        private List<CheckRule> breakRules;

        public CheckResultBuilder setBreakRules(List<CheckRule> breakRules) {
            this.breakRules = breakRules;
            return this;
        }

        public CheckResultBuilder addBreakRules(List<CheckRule> rules) {
            breakRules.addAll(rules);
            return this;
        }

        public CheckResultBuilder addBreakRule(CheckRule rule) {
            breakRules.add(rule);
            return this;
        }

        public CheckResultBuilder setMessage(Text message) {
            this.message = message;
            return this;
        }

        public CheckResultBuilder setBanned(boolean banned) {
            this.banned = banned;
            return this;
        }

        public CheckResult build() {
            return new CheckResult(banned, message, breakRules);
        }
    }
}
