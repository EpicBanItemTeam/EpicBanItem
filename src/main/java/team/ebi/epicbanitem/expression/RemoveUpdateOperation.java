/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.expression;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.ebi.epicbanitem.api.expression.UpdateOperation;

public class RemoveUpdateOperation extends AbstractMap<DataQuery, UpdateOperation> implements UpdateOperation {

    private final DataQuery query;

    public RemoveUpdateOperation(DataQuery query) {
        this.query = query;
    }

    public DataQuery query() {
        return query;
    }

    @Override
    public DataView process(DataView view) {
        return view.remove(query);
    }

    @NotNull
    @Override
    public Set<Entry<DataQuery, UpdateOperation>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.translatable("epicbanitem.operations.remove").args(Component.text(query.toString()));
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
        RemoveUpdateOperation that = (RemoveUpdateOperation) o;
        return query.equals(that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), query);
    }
}
