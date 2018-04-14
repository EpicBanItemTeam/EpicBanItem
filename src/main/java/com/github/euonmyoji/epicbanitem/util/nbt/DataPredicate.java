package com.github.euonmyoji.epicbanitem.util.nbt;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

import java.util.Map;
import java.util.Optional;

/**
 * @author ustc_zzzz
 */
public interface DataPredicate {
    Optional<Map<String, DataPredicate>> testAndGetArrayPlaceholderFinder(DataQuery query, DataView view);
}
