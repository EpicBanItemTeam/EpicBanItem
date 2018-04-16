package com.github.euonmyoji.epicbanitem.check;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author yinyangshi
 * 检查物品的结果
 * the result of check
 */
public class CheckResult {
    private boolean banned;
    private Text message;
    private List<CheckRule> breakRules;
    private Location from;
    private World world;

    private CheckResult(CheckResultBuilder builder) {
        this.banned = builder.banned;
        this.message = builder.message;
        this.breakRules = builder.breakRules;
        this.from = builder.from;
        this.world = builder.world;
    }

    public orElse ifBanned(Consumer<CheckResult> consumer) {
        if (banned)
            consumer.accept(this);
        return new orElse(!banned);
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(this.world);
    }

    public Optional<Location> getFrom() {
        return Optional.ofNullable(this.from);
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
        private List<CheckRule> breakRules = new ArrayList<>();
        private Location from;
        private World world;

        public CheckResultBuilder setWorld(World world) {
            this.world = world;
            return this;
        }

        public CheckResultBuilder setBreakRules(List<CheckRule> breakRules) {
            this.breakRules = breakRules;
            return this;
        }

        public CheckResultBuilder addBreakRules(List<CheckRule> rules) {
            breakRules.addAll(rules);
            return this;
        }

        public CheckResultBuilder setFrom(Location from) {
            this.from = from;
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
            return new CheckResult(this);
        }
    }

    public class orElse {
        private boolean execute;

        private orElse(boolean execute) {
            this.execute = execute;
        }

        @SuppressWarnings("MethodNameSameAsClassName")
        public void orElse(Runnable r) {
            if (execute)
                r.run();
        }
    }
}
