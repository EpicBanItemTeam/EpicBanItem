package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
@SuppressWarnings("WeakerAccess")
public final class UpdateResult {
    private final Map<String, Operation> children;

    private UpdateResult(Map<String, Operation> children) {
        this.children = ImmutableMap.copyOf(children);
    }

    public static UpdateResult nothing() {
        return new UpdateResult(ImmutableMap.of());
    }

    public static UpdateResult update(Map<String, Operation> fields) {
        return new UpdateResult(fields);
    }

    private void apply(DataQuery query, DataView view) {
        this.children.forEach((k, v) -> {
            if (v.replace) {
                Optional<?> value = (Optional<?>) v.value;
                NbtTypeHelper.setObject(query.then(k), view, o -> value.orElse(null));
            } else {
                UpdateResult value = (UpdateResult) v.value;
                value.apply(query.then(k), view);
            }
        });
    }

    public void apply(DataView view) {
        this.apply(DataQuery.of(), view);
    }

    public Map<String, Operation> getChildren() {
        return this.children;
    }

    public UpdateResult merge(Map<String, Operation> anotherChildren) {
        Map<String, Operation> children = new LinkedHashMap<>(this.children);
        for (Map.Entry<String, Operation> entry : anotherChildren.entrySet()) {
            Operation value = entry.getValue();
            children.compute(entry.getKey(), (k, v) -> {
                if (Objects.isNull(v)) {
                    return value;
                } else if (!v.replace && !value.replace) {
                    return Operation.update(((UpdateResult) v.value).merge(((UpdateResult) value.value).getChildren()));
                } else {
                    throw new IllegalArgumentException("Find two apply operations for the same field: \"" + k + "\"");
                }
            });
        }
        return new UpdateResult(children);
    }

    @Override
    public String toString() {
        String nextSeparator = "";
        StringBuilder sb = new StringBuilder("UpdateResult{");
        for (Map.Entry<String, Operation> entry : this.children.entrySet()) {
            sb.append(nextSeparator).append(entry.getKey());
            sb.append('=').append(entry.getValue());
            nextSeparator = ", ";
        }
        return sb.append('}').toString();
    }

    public static class Operation {
        private final boolean replace;
        private final Object value; // could be either UpdateResult or Optional<?>

        private Operation(boolean replace, Object value) {
            this.replace = replace;
            this.value = value;
        }

        public static Operation remove() {
            return new Operation(true, Optional.empty());
        }

        public static Operation update(UpdateResult result) {
            return new Operation(false, result);
        }

        public static Operation replace(Object newValue) {
            return new Operation(true, Optional.of(newValue));
        }

        public Optional<UpdateResult> getUpdateResult() {
            return this.replace ? Optional.empty() : Optional.of((UpdateResult) this.value);
        }

        @Override
        public String toString() {
            if (this.replace) {
                return ((Optional<?>) this.value).map(o -> "ReplaceResult{value=" + o + "}").orElse("RemoveResult{}");
            } else {
                return ((UpdateResult) this.value).toString();
            }
        }
    }
}
