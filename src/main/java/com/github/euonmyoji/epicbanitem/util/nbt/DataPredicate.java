package com.github.euonmyoji.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

import java.util.Optional;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
@FunctionalInterface
public interface DataPredicate {
    /**
     * query
     *
     * @param query data query of something
     * @param view  data view of something
     * @return query result, if present
     */
    Optional<QueryResult> query(DataQuery query, DataView view);
}
