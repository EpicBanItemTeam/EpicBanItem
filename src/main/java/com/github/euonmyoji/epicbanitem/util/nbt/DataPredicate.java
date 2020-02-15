package com.github.euonmyoji.epicbanitem.util.nbt;

import java.util.Optional;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.util.Tristate;

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

    /**
     * filter string first for indexing
     *
     * @param query the query
     * @param value the string value of the corresponding query
     * @return {@link Tristate#TRUE} or {@link Tristate#FALSE} if it is certain
     * whether return values of the method {@link #query(DataQuery, DataView)}
     * are present or absent, or {@link Tristate#UNDEFINED} otherwise
     */
    default Tristate filterString(DataQuery query, String value) {
        return Tristate.UNDEFINED;
    }
}
