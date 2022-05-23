/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.*;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class ReplaceUpdateOperation extends AbstractMap<DataQuery, UpdateOperation> implements UpdateOperation {

    private final Object value;
    private final DataQuery query;

    public ReplaceUpdateOperation(DataQuery query, Object value) {
        this.query = query;
        this.value = value;
    }

    public Object value() {
        return value;
    }

    public DataQuery query() {
        return query;
    }

    @Override
    public DataView process(DataView view) {
        return view.set(query, value);
    }

    @NotNull
    @Override
    public Set<Entry<DataQuery, UpdateOperation>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.translatable("epicbanitem.operations.replace")
                .args(Component.text(query.toString()), Component.text(value.toString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ReplaceUpdateOperation that = (ReplaceUpdateOperation) o;
        if (value instanceof int[] o1 && that.value instanceof int[] o2) {
            return Arrays.equals(o1, o2) && query.equals(that.query);
        }
        if (value instanceof byte[] o1 && that.value instanceof byte[] o2) {
            return Arrays.equals(o1, o2) && query.equals(that.query);
        }
        if (value instanceof long[] o1 && that.value instanceof long[] o2) {
            return Arrays.equals(o1, o2) && query.equals(that.query);
        }
        return value.equals(that.value) && query.equals(that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, query);
    }
}
