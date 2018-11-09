package com.github.euonmyoji.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@FunctionalInterface
public interface DataPredicate {
    Optional<QueryResult> query(DataQuery query, DataView view);
}
