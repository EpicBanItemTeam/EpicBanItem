package com.github.euonmyoji.epicbanitem.check;

import com.github.euonmyoji.epicbanitem.util.nbt.UpdateExpression;
import org.spongepowered.api.world.Location;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author yinyangshi
 * 检查物品的结果
 * the result of checkItemStack
 */
public class CheckResult {
    private boolean banned;
    private boolean remove;
    private CheckRule breakRule;
    private Location from;
    private String world;
    private Object checkedObject;
    private UpdateExpression updateExpression;

    private CheckResult(Builder builder) {
        this.banned = builder.banned;
        this.breakRule = builder.breakRule;
        this.from = builder.from;
        this.world = builder.world;
        this.checkedObject = builder.checkedObject;
        this.remove = builder.remove;
        this.updateExpression = builder.updateExpression;
    }

    public Optional<UpdateExpression> getUpdateExpression() {
        return Optional.ofNullable(this.updateExpression);
    }

    public boolean shouldRemove() {
        return this.remove;
    }

    public Object getCheckedObject() {
        return this.checkedObject;
    }

    public Else ifBanned(Consumer<CheckResult> consumer) {
        if (banned)
            consumer.accept(this);
        return new Else(!banned);
    }

    public Optional<String> getWorld() {
        return Optional.ofNullable(this.world);
    }

    public Optional<Location> getFrom() {
        return Optional.ofNullable(this.from);
    }

    public Optional<CheckRule> getBreakRule() {
        return Optional.ofNullable(this.breakRule);
    }

    public boolean isBanned() {
        return this.banned;
    }


    public static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private boolean banned;
        private CheckRule breakRule;
        private Location from;
        private String world;
        private Object checkedObject;
        private boolean remove;
        private UpdateExpression updateExpression;

        Builder setUpdateExpression(UpdateExpression updateExpression) {
            this.updateExpression = updateExpression;
            return this;
        }

        Builder setRemove(boolean remove) {
            this.remove = remove;
            return this;
        }

        Builder setWorld(String world) {
            this.world = world;
            return this;
        }

        Builder setCheckedObject(Object checkedObject) {
            this.checkedObject = checkedObject;
            return this;
        }

        Builder setBreakRule(CheckRule breakRule) {
            this.breakRule = breakRule;
            return this;
        }

        Builder setFrom(Location from) {
            this.from = from;
            return this;
        }

        Builder setBanned(boolean banned) {
            this.banned = banned;
            return this;
        }

        CheckResult build() {
            return new CheckResult(this);
        }
    }

    public class Else {
        private boolean execute;

        private Else(boolean execute) {
            this.execute = execute;
        }

        public void orElse(Runnable r) {
            if (execute)
                r.run();
        }
    }
}
