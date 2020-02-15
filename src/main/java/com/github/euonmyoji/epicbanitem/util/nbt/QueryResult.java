package com.github.euonmyoji.epicbanitem.util.nbt;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@NonnullByDefault
@SuppressWarnings("WeakerAccess")
public final class QueryResult {
    private final boolean isArray;
    private final boolean isObject;
    private final Map<String, QueryResult> children;

    private QueryResult(boolean isArray, boolean isObject, Map<String, QueryResult> children) {
        this.children = ImmutableMap.copyOf(children);
        this.isObject = isObject;
        this.isArray = isArray;
    }

    public static Optional<QueryResult> check(boolean condition) {
        return condition ? success() : failure();
    }

    public static Optional<QueryResult> failure() {
        return Optional.empty();
    }

    public static Optional<QueryResult> success() {
        return Optional.of(new QueryResult(false, false, ImmutableMap.of()));
    }

    public static Optional<QueryResult> successArray(Map<String, QueryResult> children) {
        return Optional.of(new QueryResult(true, false, children));
    }

    public static Optional<QueryResult> successObject(Map<String, QueryResult> children) {
        return Optional.of(new QueryResult(false, true, children));
    }

    public boolean isArrayChildren() {
        return this.isArray;
    }

    public boolean isObjectChildren() {
        return this.isObject;
    }

    public Map<String, QueryResult> getChildren() {
        return this.children;
    }

    public QueryResult merge(Map<String, QueryResult> anotherChildren) {
        Map<String, QueryResult> children = new LinkedHashMap<>(this.children);
        for (Map.Entry<String, QueryResult> entry : anotherChildren.entrySet()) {
            QueryResult value = entry.getValue();
            children.compute(
                entry.getKey(),
                (k, v) -> {
                    if (Objects.isNull(v)) {
                        return value;
                    } else {
                        return v.merge(value.getChildren());
                    }
                }
            );
        }
        return new QueryResult(this.isArray, this.isObject, children);
    }

    @Override
    public String toString() {
        String str = "{";
        String nextSeparator = "";
        str = this.isArrayChildren() ? "Array{" : str;
        str = this.isObjectChildren() ? "Object{" : str;
        StringBuilder sb = new StringBuilder("QueryResult").append(str);
        for (Map.Entry<String, QueryResult> entry : this.children.entrySet()) {
            sb.append(nextSeparator).append(entry.getKey());
            sb.append('=').append(entry.getValue());
            nextSeparator = ", ";
        }
        return sb.append('}').toString();
    }
}
