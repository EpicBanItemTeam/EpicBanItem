/*
 * Copyright 2022 EpicBanItem Team. All Rights Reserved.
 *
 * This file is part of EpicBanItem, licensed under the GNU GENERAL PUBLIC LICENSE Version 3 (GPL-3.0)
 */
package team.ebi.epicbanitem.api.expression;

import java.util.Map;
import java.util.Objects;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Nullable;
import team.ebi.epicbanitem.expression.CommonUpdateOperation;
import team.ebi.epicbanitem.expression.RemoveUpdateOperation;
import team.ebi.epicbanitem.expression.ReplaceUpdateOperation;

public interface UpdateOperation extends Map<DataQuery, UpdateOperation>, ComponentLike {

    static UpdateOperation common() {
        return common(Maps.newHashMap());
    }

    static UpdateOperation common(Map<DataQuery, UpdateOperation> children) {
        return new CommonUpdateOperation(children);
    }

    static UpdateOperation remove(DataQuery query) {
        return new RemoveUpdateOperation(query);
    }

    static UpdateOperation replace(DataQuery query, Object value) {
        return new ReplaceUpdateOperation(query, value);
    }

    /**
     * @param view {@link DataView} to update
     * @return The same {@link DataView} object that is updated
     */
    DataView process(DataView view);

    default UpdateOperation merge(@Nullable UpdateOperation another) {
        return Objects.isNull(another) ? this : another;
    }
}
